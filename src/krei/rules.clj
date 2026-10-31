;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.rules
  "Rules describe files a repository holds. A rule is an EDN map:

    {:doc \"…\"
     :when [:license]           ; facts that must be set
     :unless [:keep-license]    ; facts that must not be set
     :files {\"LICENSE\" {:copy \"LICENSES/{{license}}.txt\"}}}

  A file is one of {:copy path-in-repository}, {:template path-beside-rule},
  {:verbatim path-beside-rule}, {:text string} or :absent. A verbatim file
  with :optional true is left alone when the rule holds no such file. Paths, copy
  sources, templates and text are Selmer templates over the facts; a
  verbatim file is written as it is. An :absent path may be a glob, which
  deletes every file it matches. :each names a fact holding a list: the
  files are then rendered once per element, bound to {{each}}. Facts in
  :skip name rules that do not apply to a repository."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [krei.facts :as facts]))

(defn- rule-file [rule-dirs rule]
  (let [file (str (name rule) ".edn")]
    (or (some #(let [p (fs/path % file)] (when (fs/exists? p) {:source (str p) :dir (str (fs/parent p))})) rule-dirs)
        (when-let [resource (io/resource (str "krei/rules/" file))]
          {:source resource :resource-dir "krei/rules"}))))

(defn load-rule
  "The rule named `rule`, from the first of `rule-dirs` holding it, else
  from the rules krei ships."
  [rule-dirs rule]
  (let [{:keys [source] :as location} (rule-file rule-dirs rule)]
    (when-not location
      (throw (ex-info (str "no rule " (name rule)) {:krei/problem :config})))
    (merge (edn/read-string (slurp source))
           (dissoc location :source)
           {:id (keyword (name rule))})))

(defn load-rules [{:keys [rules rule-dirs]}]
  (mapv #(load-rule rule-dirs %) rules))

(defn applies? [{:keys [id when unless]} facts]
  (and (every? #(some? (get facts %)) when)
       (not-any? #(get facts %) unless)
       (not (contains? (set (:skip facts)) id))))

(defn relative-path
  "`path` when it names a file inside the repository, an error otherwise."
  [path]
  (let [p (fs/path path)]
    (when (or (str/blank? path) (fs/absolute? p) (some #{".." "." ".git"} (map str (seq p))))
      (throw (ex-info (str "the path " path " leaves the repository") {:krei/problem :rule})))
    path))

(defn- template-text
  "The text of the file `template` beside the rule, or nil when there is none."
  [{:keys [dir resource-dir]} template]
  (relative-path template)
  (if dir
    (let [p (fs/path dir template)] (when (fs/regular-file? p) (slurp (str p))))
    (some-> (io/resource (str resource-dir "/" template)) slurp)))

(defn- beside-rule [rule template]
  (or (template-text rule template)
      (throw (ex-info (str "no template " template) {:krei/problem :rule}))))

(defn- copied
  "The bytes of `source`, as another rule writes it in this run if one does,
  else as the checkout at `dir` holds it."
  [dir source pending]
  (let [planned (get pending source)
        path (fs/path dir source)]
    (cond
      (bytes? planned) planned
      (fs/regular-file? path) (fs/read-all-bytes path)
      :else (throw (ex-info (str "no file " source " to copy") {:krei/problem :rule})))))

(defn- preserved
  "`text` with every match of each `patterns` regex replaced by the match of
  the existing file at `path` whose first group is the same, so a value such
  as a pinned version stays as the repository has it once the file exists."
  [text dir path patterns]
  (let [p (fs/path dir path)]
    (if (and (seq patterns) (fs/regular-file? p {:nofollow-links true}))
      (let [existing (slurp (str p))]
        (reduce (fn [text pattern]
                  (let [re (re-pattern pattern)
                        kept (into {} (for [[whole k] (re-seq re existing)] [k whole]))]
                    (str/replace text re (fn [[whole k]] (get kept k whole)))))
                text
                patterns))
      text)))

(defn- content
  "The bytes `spec` puts into the file `path` of the checkout at `dir`, or :absent."
  [rule dir path spec facts pending]
  (cond
    (= :absent spec) :absent
    (:copy spec) (copied dir (relative-path (facts/render (:copy spec) facts)) pending)
    (:template spec) (.getBytes ^String (preserved (facts/render (beside-rule rule (:template spec)) facts) dir path (:preserve spec)) "UTF-8")
    (:verbatim spec) (let [path (facts/render (:verbatim spec) facts)]
                       (if-let [text (template-text rule path)]
                         (.getBytes ^String text "UTF-8")
                         (if (:optional spec)
                           :skip
                           (throw (ex-info (str "no template " path) {:krei/problem :rule})))))
    (contains? spec :text) (.getBytes ^String (facts/render (:text spec) facts) "UTF-8")
    :else (throw (ex-info (str "the rule " (name (:id rule)) " holds an unknown file " (pr-str spec)) {:krei/problem :rule}))))

(defn- current [dir path]
  (let [p (fs/path dir path)]
    (when (fs/exists? p {:nofollow-links true})
      (if (fs/regular-file? p {:nofollow-links true}) (fs/read-all-bytes p) :other))))

(defn- comparable [content]
  (if (bytes? content) (vec content) content))

(defn- glob? [path]
  (boolean (re-find #"[*?\[{]" path)))

(defn- matching
  "The files of the checkout at `dir` the glob `pattern` matches."
  [dir pattern]
  (->> (fs/glob dir pattern {:hidden true})
       (filter #(fs/regular-file? % {:nofollow-links true}))
       (map #(str (fs/relativize dir %)))
       (remove #(some #{".git"} (map str (seq (fs/path %)))))
       sort))

(defn- entries
  "Every [path spec facts] that `rule` holds for the facts `facts`, with
  :each expanded and :absent globs resolved against the checkout at `dir`."
  [rule dir facts]
  (for [scope (if-let [each (:each rule)]
                (map #(assoc facts :each %) (get facts each))
                [facts])
        [path spec] (sort-by key (:files rule))
        :let [rendered (facts/render path scope)]
        target (if (and (= :absent spec) (glob? rendered))
                 (matching dir rendered)
                 [(relative-path rendered)])]
    [target spec scope]))

(defn changes
  "What the checkout at `dir` needs for `rules` to hold, given `facts`:
  [{:rule :path :action :content}] with :action one of :create, :update or
  :delete. Two rules disagreeing about one file is an error. A copy reads
  what the other rules write in the same run, so it never lags a run behind."
  [rules dir facts]
  (let [claims (for [rule rules
                     :when (applies? rule facts)
                     [path spec scope] (entries rule dir facts)]
                 {:rule rule :path path :spec spec :facts scope})
        copy? #(and (map? (:spec %)) (:copy (:spec %)))
        resolve (fn [pending {:keys [rule path spec facts]}]
                  {:rule (:id rule) :path path :content (content rule dir path spec facts pending)})
        pending (into {} (map (juxt :path :content)) (map #(resolve {} %) (remove copy? claims)))
        wanted (remove #(= :skip (:content %)) (map #(resolve (if (copy? %) pending {}) %) claims))
        by-path (group-by :path wanted)]
    (doseq [[path claims] by-path
            :when (< 1 (count (distinct (map (comp comparable :content) claims))))]
      (throw (ex-info (str "the rules " (str/join ", " (map (comp name :rule) claims)) " disagree about " path)
                      {:krei/problem :rule})))
    (->> (distinct (map :path wanted))
         (map #(first (by-path %)))
         (keep (fn [{:keys [path content] :as change}]
                 (let [existing (current dir path)]
                   (cond
                     (and (= :absent content) existing) (assoc change :action :delete)
                     (= :absent content) nil
                     (nil? existing) (assoc change :action :create)
                     (= (comparable existing) (comparable content)) nil
                     :else (assoc change :action :update)))))
         vec)))

(defn- inside! [dir p]
  (let [root (str (fs/canonicalize dir))
        target (str (fs/canonicalize p))]
    (when-not (or (= root target) (str/starts-with? target (str root "/")))
      (throw (ex-info (str "the path " p " leaves the repository") {:krei/problem :rule})))))

(defn apply!
  "Writes `changes` into the checkout at `dir`."
  [dir changes]
  (doseq [{:keys [path action content]} changes
          :let [p (fs/path dir path)]]
    (fs/create-dirs (fs/parent p))
    (inside! dir (fs/parent p))
    (when (fs/exists? p {:nofollow-links true})
      (fs/delete-tree p))
    (when-not (= :delete action)
      (fs/write-bytes p content))))

(defn- first-match
  "The 1-based line of the first match of `pattern` in `text`, or nil."
  [pattern text]
  (let [m (re-matcher (re-pattern pattern) text)]
    (when (.find m)
      (inc (count (filter #{\newline} (subs text 0 (.start m))))))))

(defn violations
  "What the checkout at `dir` breaks of the checks in `rules`, which krei
  reports but cannot fix: a fact of :expect with another value, and a match
  of a :forbid pattern in a file its glob selects. [{:rule :path :message}]"
  [rules dir facts]
  (vec
   (concat
    (for [rule rules
          :when (applies? rule facts)
          [fact wanted] (sort-by key (:expect rule))
          :let [actual (get facts fact)]
          :when (not= wanted actual)]
      {:rule (:id rule) :message (str (name fact) " is " (pr-str actual) ", expected " (pr-str wanted))})
    (for [rule rules
          :when (applies? rule facts)
          [glob patterns] (sort-by key (:forbid rule))
          path (matching dir glob)
          :let [text (slurp (str (fs/path dir path)))]
          pattern patterns
          :let [line (first-match pattern text)]
          :when line]
      {:rule (:id rule) :path path :message (str path ":" line " matches " pattern)}))))
