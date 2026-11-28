;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.golden-test
  "One scenario run end to end and compared with test/krei/golden/expected.edn:
  an inventory in the format aro writes, a configuration and rule directory
  found through the XDG variables, and repositories served from local bare
  clones. The command line runs as a subprocess with its own HOME and XDG
  directories, so it reads nothing of the machine's own krei or aro state.

  The scenario's repositories live on the host forge.invalid, which git
  rewrites to the local bare clones; a missing rewrite fails to resolve the
  host instead of reaching a forge, and the host is no GitHub host, so sync
  never calls gh.

  Setting KREI_GOLDEN_WRITE=1 rewrites expected.edn from the current code.
  A rewritten file that differs is a change of behaviour, and belongs in the
  pull request that makes it."
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [clojure.walk :as walk]
            [krei.calendar :as calendar]
            [krei.config :as config]
            [krei.facts :as facts]
            [krei.git :as git]
            [krei.inventory :as inventory]
            [krei.rules :as rules]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(def ^:private golden "test/krei/golden")

(def ^:private repositories
  "The default branch of each scenario repository: path → {file content},
  where content is a string, [:exec string] or [:link target]."
  {"metio/aro" {"LICENSES/0BSD.txt" "Zero-Clause BSD\n"
                "README.md" "# aro\n\n## License\n\n0BSD\n"
                "run.sh" [:exec "#!/bin/sh\nold --flag\n"]
                "NOTICE" [:link "README.md"]
                "config/a.edn" "{}\n"
                ".idea/workspace.xml" "<project/>\n"
                ".krei.edn" ";; own facts\n{:support \"community\"}\n"}
   "metio/blank" {"LICENSES/CC0-1.0.txt" "CC0\n"
                  ".krei.edn" ";; nothing to add yet\n"}
   "metio/krei" {"LICENSES/CC0-1.0.txt" "CC0\n"
                 "README.md" "# krei\n"
                 ".krei.edn" (pr-str {:skip [:license :editorconfig :readme-mirrors :golden-notice
                                             :golden-script :golden-dir :golden-expect]})}})

(defn- write-tree! [dir files]
  (doseq [[file content] files
          :let [p (fs/path dir file)]]
    (fs/create-dirs (fs/parent p))
    (if (vector? content)
      (let [[kind value] content]
        (case kind
          :exec (do (spit (str p) value) (fs/set-posix-file-permissions p "rwxr-xr-x"))
          :link (fs/create-sym-link p value)))
      (spit (str p) content))))

(defn- remote [path]
  (support/path "remotes" path))

(defn- scenario!
  "Bare clones of the repositories, the inventory, config and rules below
  the XDG directories, and a git config rewriting forge.invalid to the clones."
  []
  (doseq [[path files] repositories
          :let [work (support/path "seed" path)]]
    (fs/create-dirs work)
    (support/git work "init" "--quiet")
    (write-tree! work files)
    ;; --force, so an ignore file of the machine running the test cannot drop a file
    (support/git work "add" "--all" "--force")
    (support/git work "commit" "--quiet" "--message" "initial")
    (fs/create-dirs (fs/parent (remote path)))
    (support/git support/*root* "clone" "--quiet" "--bare" work (remote path)))
  (spit (support/path "gitconfig")
        (str "[url \"" (support/path "remotes") "/\"]\n\tinsteadOf = forge.invalid:\n")
        :append true)
  (let [state (support/path "state" "aro")
        rules (support/path "config" "krei" "rules")]
    (fs/create-dirs state)
    (fs/create-dirs rules)
    (fs/copy (fs/path golden "inventory.edn") (fs/path state "inventory.edn"))
    (fs/copy (fs/path golden "config.edn") (fs/path (fs/parent rules) "config.edn"))
    (doseq [f (fs/list-dir (fs/path golden "rules"))]
      (fs/copy f (fs/path rules (fs/file-name f))))))

(defn- environment []
  (merge git/*environment*
         {"HOME" (support/path "home")
          "XDG_CONFIG_HOME" (support/path "config")
          "XDG_STATE_HOME" (support/path "state")
          "XDG_CACHE_HOME" (support/path "cache")}))

(defn- normalized
  "x with every occurrence of the scenario's root replaced by <root>."
  [x]
  (walk/postwalk #(if (string? %) (str/replace % support/*root* "<root>") %) x))

(defn- krei
  "The exit code and output of the krei command line run with `args`."
  [& args]
  (let [{:keys [exit out err]}
        (apply process/shell {:out :string :err :string :continue true
                              :dir support/*root* :extra-env (environment)}
               "bb" "--config" (str (fs/absolutize "bb.edn")) "-m" "krei.main" args)]
    (normalized {:exit exit :out (str/split-lines out) :err (str/split-lines err)})))

(defn- tree
  "Every file below `dir` but .git: {path {:type :exec? :content}}."
  [dir]
  (into (sorted-map)
        (for [p (fs/glob dir "**" {:hidden true})
              :let [rel (str (fs/relativize dir p))]
              :when (not (or (= ".git" rel) (str/starts-with? rel ".git/")))
              :when (not (fs/directory? p {:nofollow-links true}))]
          [rel (if (fs/sym-link? p)
                 {:type :link :target (str (fs/read-link p))}
                 {:type :file
                  :exec? (contains? (fs/posix-file-permissions p) java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE)
                  :content (slurp (str p))})])))

(defn- remote-branch [path branch]
  (let [git-dir (remote path)]
    {:message (support/git support/*root* "--git-dir" git-dir "log" "-1" "--format=%B" branch)
     :files (str/split-lines (support/git support/*root* "--git-dir" git-dir "ls-tree" "-r" branch))}))

(defn- in-process
  "What the namespaces read and plan from the scenario files, called
  directly."
  []
  (let [config (config/load-config (support/path "config" "krei" "config.edn"))
        config (assoc config :rule-dirs [(support/path "config" "krei" "rules")])
        all (inventory/read-inventory (support/path "state" "aro" "inventory.edn"))
        selected (inventory/select config all nil)
        calendar (calendar/calendar config selected)
        config (assoc config :calendar calendar)
        loaded (rules/load-rules config)
        per-repository (into (sorted-map)
                             (for [{:keys [id path] :as repository} selected
                                   :let [dir (support/path "checkout" path)]]
                               (do (support/git support/*root* "clone" "--quiet" (remote path) dir)
                                   (let [known (facts/facts config repository dir)
                                         changes (rules/changes loaded dir known)]
                                     [id {:facts known
                                          :changes (mapv #(update % :content (fn [c] (if (bytes? c) (String. ^bytes c "UTF-8") c)))
                                                         changes)
                                          :violations (rules/violations loaded dir known changes)}]))))]
    (normalized
     {:config (dissoc config :inventory :rule-dirs :cache :calendar)
      :inventory all
      :selected (mapv :id selected)
      :calendar calendar
      :rules loaded
      :repositories per-repository})))

(defn- run-scenario []
  (scenario!)
  (let [before (in-process)
        commands {:rules (krei "rules")
                  :list (krei "list")
                  :list-filtered (krei "list" "forge.invalid/metio/a*" "nothing")
                  :plan (krei "plan")
                  :check (krei "check")}
        work (support/path "work" "aro")
        _ (support/git support/*root* "clone" "--quiet" (remote "metio/aro") work)
        applying (krei "apply" work "aro")
        applied (normalized (tree work))
        sync (krei "sync")
        synced {:aro (remote-branch "metio/aro" "krei/sync")
                :blank (remote-branch "metio/blank" "krei/sync")}
        resync (krei "sync")
        _ (fs/delete (support/path "state" "aro" "inventory.edn"))
        missing (krei "list")]
    {:in-process before
     :commands (assoc commands :apply applying :sync sync :resync resync :missing-inventory missing)
     :applied applied
     :synced synced}))

(deftest the-golden-scenario-gives-the-recorded-results
  (let [actual (run-scenario)
        file (fs/path golden "expected.edn")]
    (if (= "1" (System/getenv "KREI_GOLDEN_WRITE"))
      ;; REUSE-IgnoreStart
      (spit (str file) (str ";; SPDX-FileCopyrightText: The krei Authors\n"
                            ";; SPDX-License-Identifier: CC0-1.0\n\n"
                            (with-out-str (pprint/pprint actual))))
      ;; REUSE-IgnoreEnd
      (let [expected (edn/read-string (slurp (str file)))]
        (doseq [k (keys expected)]
          (is (= (get expected k) (get actual k)) (str k " differs from " file)))
        (is (= (set (keys expected)) (set (keys actual))))))))
