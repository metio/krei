;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.rules
  "Rules describe files a repository holds. A rule is an EDN map:

    {:doc \"…\"
     :when [:license]           ; facts that must be set
     :unless [:keep-license]    ; facts that must not be set
     :files {\"LICENSE\" {:copy \"LICENSES/{{license}}.txt\"}}}

  A file is one of {:copy path-in-repository}, {:template path-beside-rule},
  {:verbatim path-beside-rule}, {:text string}, :absent, or an edit of the file in place,
  {:replace [[pattern replacement] …]} or {:markdown …} (see krei.markdown). A verbatim file
  with :optional true is left alone when the rule holds no such file. Paths, copy
  sources, templates and text are Selmer templates over the facts; a
  verbatim file is written as it is. An :absent path may be a glob, which
  deletes every file it matches. :each names a fact holding a list: the
  files are then rendered once per element, bound to {{each}}. Facts in
  :skip name rules that do not apply to a repository."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [krei.facts :as facts]
            [krei.markdown :as markdown]))

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

(defn applies?
  "Whether the rule holds for `facts`: every fact of :when is set, none of
  :unless is, and the repository does not skip it. A fact is set when it
  has a value other than false, so {:maven-verify false} opts out on both
  sides."
  [{:keys [id when unless]} facts]
  (and (every? #(get facts %) when)
       (not-any? #(get facts %) unless)
       (not (contains? (set (:skip facts)) id))))

(defn relative-path
  "`path` when it names a file inside the repository, an error otherwise."
  [path]
  (let [p (fs/path path)]
    (when (or (str/blank? path) (fs/absolute? p) (some #{".." "." ".git"} (map str (seq p))))
      (throw (ex-info (str "the path " path " leaves the repository") {:krei/problem :rule})))
    path))

(defn- inside?
  "Whether `p`, with every symlink on its way resolved, lies in `dir`."
  [dir p]
  (let [root (str (fs/canonicalize dir))
        target (str (fs/canonicalize p))]
    (or (= root target) (str/starts-with? target (str root "/")))))

(defn- checkout-file?
  "Whether `p` is a regular file of the checkout at `dir`: not a symlink, and
  not reached through a symlinked directory, so krei never reads, and then
  commits, a file from outside the repository."
  [dir p]
  (and (fs/regular-file? p {:nofollow-links true}) (inside? dir p)))

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
      (checkout-file? dir path) (fs/read-all-bytes path)
      :else (throw (ex-info (str "no file " source " to copy") {:krei/problem :rule})))))

(defn- preserved
  "`text` with every match of each `patterns` regex replaced by the match of
  the existing file at `path` whose first group is the same, so a value such
  as a pinned version stays as the repository has it once the file exists."
  [text dir path patterns]
  (let [p (fs/path dir path)]
    (if (and (seq patterns) (checkout-file? dir p))
      (let [existing (slurp (str p))]
        (reduce (fn [text pattern]
                  (let [re (re-pattern pattern)
                        kept (into {} (for [[whole k] (re-seq re existing)] [k whole]))]
                    (str/replace text re (fn [[whole k]] (get kept k whole)))))
                text
                patterns))
      text)))

(defn- current [dir path]
  (let [p (fs/path dir path)]
    (when (fs/exists? p {:nofollow-links true})
      (if (checkout-file? dir p) (fs/read-all-bytes p) :other))))

(defn- utf-8
  "The text of `bytes`, or nil when they are no valid UTF-8."
  [^bytes bytes]
  (try
    (str (.decode (-> java.nio.charset.StandardCharsets/UTF_8
                      .newDecoder
                      (.onMalformedInput java.nio.charset.CodingErrorAction/REPORT)
                      (.onUnmappableCharacter java.nio.charset.CodingErrorAction/REPORT))
                  (java.nio.ByteBuffer/wrap bytes)))
    (catch java.nio.charset.CharacterCodingException _ nil)))

(defn- edited
  "The file `path` as the other rules of the run leave it, or as the
  checkout at `dir` holds it, with `f` applied to its text. A file that is
  no valid UTF-8 keeps its bytes when `f` changes nothing, and is an error
  otherwise, since writing the text back would replace every undecodable
  byte with U+FFFD."
  [dir path f pending]
  (let [before (get pending path (current dir path))]
    (if (bytes? before)
      (if-let [text (utf-8 before)]
        (let [after (f text)]
          (if (= text after) before (.getBytes ^String after "UTF-8")))
        (let [text (String. ^bytes before "UTF-8")]
          (if (= text (f text))
            before
            (throw (ex-info (str path " is no valid UTF-8, so it cannot be edited") {:krei/problem :rule})))))
      :skip)))

(defn- group-refs
  "`replacement` with each group reference ($1, ${name}) swapped for a marker
  that rendering leaves alone, and the references in marker order. As in
  java.util.regex, a $ takes the most digits that still name a group of
  `re`, and a backslash makes the next character literal."
  [replacement re]
  (let [groups (.groupCount (re-matcher re ""))
        refs (atom [])
        mark (fn [ref] (swap! refs conj ref) (str \uE000 (dec (count @refs)) \uE001))
        marked (str/replace replacement #"\\(.)|\$(\d+)|\$\{([A-Za-z][A-Za-z0-9]*)\}"
                            (fn [[_ literal digits named]]
                              (cond
                                literal literal
                                named (mark named)
                                :else (let [n (or (last (filter #(<= (parse-long %) groups)
                                                                (map #(subs digits 0 %) (range 1 (inc (count digits))))))
                                                  (subs digits 0 1))]
                                        (str (mark (parse-long n)) (subs digits (count n)))))))]
    [marked @refs]))

(defn- replaced
  "`text` with each [pattern replacement] of `replacements` applied to every
  match; a replacement refers to the groups of its pattern as $1, $2 and so on.
  Only the replacement as the rule writes it refers to groups: the facts it
  renders are written as they are, whatever $ or \\ they hold."
  [text replacements facts]
  (reduce (fn [text [pattern replacement]]
            (let [re (re-pattern pattern)
                  [marked refs] (group-refs replacement re)
                  rendered (facts/render marked facts)
                  m (re-matcher re text)
                  out (StringBuilder.)]
              (while (.find m)
                (.appendReplacement m out (java.util.regex.Matcher/quoteReplacement
                                           (str/replace rendered #"\uE000(\d+)\uE001"
                                                        (fn [[_ i]]
                                                          (let [ref (nth refs (parse-long i))]
                                                            (or (if (string? ref) (.group m ^String ref) (.group m (int ref))) "")))))))
              (.appendTail m out)
              (str out)))
          text
          replacements))

(defn- rendered-body
  "`body` rendered with `facts`, or :absent when it renders blank."
  [body facts]
  (let [text (facts/render body facts)]
    (if (str/blank? text) :absent text)))

(defn- rendered-markdown
  "The :markdown `spec` with its header and section bodies rendered."
  [spec facts]
  (cond-> (update spec :sections (fn [sections]
                                   (mapv #(cond-> % (string? (:body %)) (update :body rendered-body facts)) sections)))
    (:header spec) (update :header facts/render facts)))

(defn- edit?
  "Whether `spec` edits a file in place instead of writing all of it."
  [spec]
  (boolean (and (map? spec) (or (:replace spec) (:markdown spec)))))

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
    (edit? spec) (edited dir path #(cond-> %
                                     (:replace spec) (replaced (:replace spec) facts)
                                     (:markdown spec) (markdown/edit (rendered-markdown (:markdown spec) facts)))
                         pending)
    :else (throw (ex-info (str "the rule " (name (:id rule)) " holds an unknown file " (pr-str spec)) {:krei/problem :rule}))))


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
  :each expanded and :absent and edit globs resolved against the checkout at
  `dir`. An edit of a single path stays planned whether or not the file
  exists, since another rule may write it in the same run; `edited` skips it
  when neither does."
  [rule dir facts]
  (for [scope (if-let [each (:each rule)]
                (map #(assoc facts :each %) (get facts each))
                [facts])
        [path spec] (sort-by key (:files rule))
        :let [rendered (facts/render path scope)]
        target (cond
                 (and (or (= :absent spec) (edit? spec)) (glob? rendered)) (matching dir rendered)
                 :else [(relative-path rendered)])]
    [target spec scope]))

(defn changes
  "What the checkout at `dir` needs for `rules` to hold, given `facts`:
  [{:rule :path :action :content}] with :action one of :create, :update or
  :delete. Two rules disagreeing about one file is an error. A copy reads
  what the other rules write and edit in the same run, so it never lags a
  run behind, and a replacement edits the file as they leave it, one
  replacing rule after another."
  [rules dir facts]
  (let [claims (map-indexed #(assoc %2 :order %1)
                            (for [rule rules
                                  :when (applies? rule facts)
                                  [path spec scope] (entries rule dir facts)]
                              {:rule rule :path path :spec spec :facts scope}))
        kind #(cond (not (map? (:spec %))) :write (:copy (:spec %)) :copy (edit? (:spec %)) :replace :else :write)
        resolve (fn [pending {:keys [rule path spec facts order]}]
                  {:rule (:id rule) :path path :order order :content (content rule dir path spec facts pending)})
        written (map #(resolve {} %) (filter #(= :write (kind %)) claims))
        ;; a skipped optional file writes nothing, so it hides nothing the
        ;; checkout holds at its path from the copies and edits after it
        pending (into {} (comp (remove #(= :skip (:content %))) (map (juxt :path :content))) written)
        edit (fn [[done pending] claim]
               (let [change (resolve pending claim)]
                 (if (= :skip (:content change))
                   [done pending]
                   [(assoc done (:path change) change) (assoc pending (:path change) (:content change))])))
        ;; edits run before the copies, which read the edited files, and
        ;; edits of a copied file run after it, so neither lags a run behind
        copied? (set (map :path (filter #(= :copy (kind %)) claims)))
        edits (filter #(= :replace (kind %)) claims)
        [edited-first pending] (reduce edit [{} pending] (remove (comp copied? :path) edits))
        copies (map #(resolve pending %) (filter #(= :copy (kind %)) claims))
        ;; whole-file writes and copies are compared before any edit of
        ;; their path replaces them, since an edit resolves against only one
        ;; of them
        _ (doseq [[path claims] (group-by :path (remove #(= :skip (:content %)) (concat written copies)))
                  :when (< 1 (count (distinct (map (comp comparable :content) claims))))]
            (throw (ex-info (str "the rules " (str/join ", " (map (comp name :rule) (sort-by :order claims))) " disagree about " path)
                            {:krei/problem :rule})))
        pending (into pending (map (juxt :path :content)) copies)
        [edited-last _] (reduce edit [{} pending] (filter (comp copied? :path) edits))
        replacements (merge edited-first edited-last)
        first-claim (into {} (map (juxt key (comp :order first val))) (group-by :path claims))
        wanted (->> (concat (remove #(contains? replacements (:path %)) (concat written copies))
                            (map #(assoc % :order (first-claim (:path %))) (vals replacements)))
                    (remove #(= :skip (:content %)))
                    (sort-by :order)
                    (map #(dissoc % :order)))
        by-path (group-by :path wanted)]
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
  (when-not (inside? dir p)
    (throw (ex-info (str "the path " p " leaves the repository") {:krei/problem :rule}))))

(defn apply!
  "Writes `changes` into the checkout at `dir`."
  [dir changes]
  (doseq [{:keys [path action content]} changes
          :let [p (fs/path dir path)]]
    ;; the nearest existing ancestor decides where create-dirs would write,
    ;; so it is checked before anything is created
    (inside! dir (first (filter fs/exists? (iterate fs/parent (fs/parent p)))))
    (fs/create-dirs (fs/parent p))
    (inside! dir (fs/parent p))
    ;; the path is deleted rather than written through, so a symbolic link
    ;; there is replaced instead of followed; a regular file keeps its mode
    (let [mode (when (fs/regular-file? p {:nofollow-links true})
                 (fs/posix-file-permissions p {:nofollow-links true}))]
      (when (fs/exists? p {:nofollow-links true})
        (fs/delete-tree p))
      (when-not (= :delete action)
        (fs/write-bytes p content)
        (when mode (fs/set-posix-file-permissions p mode))))))

(defn- first-match
  "The 1-based line of the first match of `pattern` in `text` whose first
  group is not in `allowed`, or nil."
  [pattern text allowed]
  (let [m (re-matcher (re-pattern pattern) text)]
    (loop []
      (when (.find m)
        (if (and (pos? (.groupCount m)) (contains? allowed (.group m 1)))
          (recur)
          (inc (count (filter #{\newline} (subs text 0 (.start m))))))))))

(defn- glob-matches? [pattern path]
  (.matches (.getPathMatcher (java.nio.file.FileSystems/getDefault) (str "glob:" pattern)) (fs/path path)))

(defn gate-problems
  "What keeps the job named `gate` in the workflow `text` from failing
  whenever another job of the workflow fails. GitHub counts a skipped job as
  passing a required check, so the gate has to run always, and it has to
  need every other job itself, since a job skipped behind a failed one
  would otherwise pass it."
  [text gate]
  (let [jobs (try (get (yaml/parse-string text :keywords false) "jobs")
                  (catch Exception _ ::invalid))
        [id job] (when (map? jobs)
                   (first (filter (fn [[id job]] (= gate (get job "name" id))) jobs)))
        needs (let [n (get job "needs")] (set (if (string? n) [n] n)))
        missing (sort (remove (conj needs id) (keys (when (map? jobs) jobs))))]
    (cond
      (= ::invalid jobs) ["is no valid YAML"]
      (nil? job) [(str "has no job named " gate)]
      :else (cond-> []
              (not (str/includes? (str (get job "if")) "always()"))
              (conj (str "runs " gate " only when every job it needs succeeds, so a failure skips it, which passes a required check; give it if: always()"))
              (seq missing)
              (conj (str "lets " gate " pass without " (str/join ", " missing) ", which it does not need"))))))

(defn violations
  "What the checkout at `dir` breaks of the checks in `rules`, which krei
  reports but cannot fix: a fact of :expect with another value, a match of
  a :forbid pattern in a file its glob selects, unless the first group of
  the match is listed in the fact :allow names, and a workflow a glob of
  :gates selects whose gate job can pass while another job fails (see
  `gate-problems`). The files are read as `changes` leave them, so a
  breach the same run fixes is none.
  [{:rule :path :message}]"
  ([rules dir facts] (violations rules dir facts []))
  ([rules dir facts changes]
  (let [changed (into {} (map (juxt :path identity)) changes)
        files (fn [glob]
                (->> (concat (matching dir glob)
                             (filter #(glob-matches? glob %) (keys changed)))
                     distinct
                     (remove #(= :delete (:action (changed %))))
                     sort))
        text (fn [path]
               (if-let [{:keys [content]} (changed path)]
                 (String. ^bytes content "UTF-8")
                 (slurp (str (fs/path dir path)))))]
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
          path (files glob)
          :let [content (text path)
                allowed (set (map str (get facts (:allow rule))))]
          pattern patterns
          :let [line (first-match pattern content allowed)]
          :when line]
      {:rule (:id rule) :path path :message (str path ":" line " matches " pattern)})
    (for [rule rules
          :when (applies? rule facts)
          [glob gate] (sort-by key (:gates rule))
          path (files glob)
          problem (gate-problems (text path) gate)]
      {:rule (:id rule) :path path :message (str path " " problem)}))))))
