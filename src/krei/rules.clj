;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.rules
  "Rules describe files a repository holds. A rule is an EDN map:

    {:doc \"…\"
     :when [:license]           ; facts that must be set
     :unless [:keep-license]    ; facts that must not be set
     :files {\"LICENSE\" {:copy \"LICENSES/{{license}}.txt\"}}}

  A file is one of {:copy path-in-repository}, {:template path-beside-rule},
  {:text string} or :absent. Paths and contents are Selmer templates over
  the facts. Facts in :skip name rules that do not apply to a repository."
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

(defn- template-text [{:keys [dir resource-dir]} template]
  (relative-path template)
  (if dir
    (slurp (str (fs/path dir template)))
    (slurp (or (io/resource (str resource-dir "/" template))
               (throw (ex-info (str "no template " template) {:krei/problem :rule}))))))

(defn- content
  "The bytes `spec` puts into a file of the checkout at `dir`, or :absent."
  [rule dir spec facts]
  (cond
    (= :absent spec) :absent
    (:copy spec) (let [source (relative-path (facts/render (:copy spec) facts))
                       path (fs/path dir source)]
                   (when-not (fs/regular-file? path)
                     (throw (ex-info (str "no file " source " to copy") {:krei/problem :rule})))
                   (fs/read-all-bytes path))
    (:template spec) (.getBytes ^String (facts/render (template-text rule (:template spec)) facts) "UTF-8")
    (contains? spec :text) (.getBytes ^String (facts/render (:text spec) facts) "UTF-8")
    :else (throw (ex-info (str "the rule " (name (:id rule)) " holds an unknown file " (pr-str spec)) {:krei/problem :rule}))))

(defn- current [dir path]
  (let [p (fs/path dir path)]
    (when (fs/exists? p {:nofollow-links true})
      (if (fs/regular-file? p {:nofollow-links true}) (fs/read-all-bytes p) :other))))

(defn- comparable [content]
  (if (bytes? content) (vec content) content))

(defn changes
  "What the checkout at `dir` needs for `rules` to hold, given `facts`:
  [{:rule :path :action :content}] with :action one of :create, :update or
  :delete. Two rules disagreeing about one file is an error."
  [rules dir facts]
  (let [wanted (for [rule rules
                     :when (applies? rule facts)
                     [path spec] (sort-by key (:files rule))]
                 {:rule (:id rule)
                  :path (relative-path (facts/render path facts))
                  :content (content rule dir spec facts)})
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
