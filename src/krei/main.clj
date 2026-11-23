;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.main
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
            [krei.calendar :as calendar]
            [krei.config :as config]
            [krei.inventory :as inventory]
            [krei.plan :as plan]
            [krei.rules :as rules]))

(def usage
  "Usage: krei [--config FILE] COMMAND [REPOSITORY…]

Commands:
  rules   List the configured rules
  list    List the managed repositories
  plan    Show what each repository needs to follow its rules
  check   Like plan, exiting non-zero when anything differs
  sync    Commit the changes onto a branch, push it and open a pull request
  apply   Write the changes of one REPOSITORY into the working tree DIR:
          krei apply DIR REPOSITORY

REPOSITORY is a repository name or a glob over host/path ids, such as
github.com/metio/*. Without one, every managed repository is used.

The config defaults to $XDG_CONFIG_HOME/krei/config.edn.")

(defn- ok [& parts] (println (str "[OK] " (str/join parts))))
(defn- skipped [& parts] (println (str "[SKIP] " (str/join parts))))
(defn- drift [& parts] (println (str "[DRIFT] " (str/join parts))))
(defn- error [& parts] (binding [*out* *err*] (println (str "[ERROR] " (str/join parts)))))

(defn- each-repository
  "Calls `f` with every selected repository, reporting failures and going on
  with the next one. Returns the results, :error for failed repositories."
  [repositories f]
  (doall
   (for [repository repositories]
     (try
       (f repository)
       ;; any failure, a file a contributor of this repository committed or
       ;; a tool missing on this machine, is this repository's alone
       (catch Exception e
         (error (:id repository) ": " (or (ex-message e) (.getName (class e))))
         :error)))))

(defn- plan! [config repositories rules]
  (each-repository
   repositories
   (fn [repository]
     (let [{:keys [changes violations]} (plan/inspect config rules repository)]
       (if (and (empty? changes) (empty? violations))
         (ok (:id repository) " follows its rules")
         (do (doseq [change changes]
               (drift (:id repository) ": " (plan/describe change)))
             (doseq [v violations]
               (drift (:id repository) ": " (plan/violation v)))))
       (if (and (empty? changes) (empty? violations)) :in-sync :drift)))))

(defn- sync-all! [config repositories rules]
  (each-repository
   repositories
   (fn [repository]
     (let [id (:id repository)
           inspection (plan/inspect config rules repository)
           _ (doseq [v (:violations inspection)]
               (drift id ": " (plan/violation v) "; fix it by hand"))
           {:keys [status pushed? pull-request opened? reason]} (plan/sync! config inspection)
           by-hand (str "; open a pull request for it by hand" (when reason (str " (" reason ")")))]
       (case status
         :in-sync (ok id " follows its rules")
         :pushed (cond
                   pushed? (ok id ": pushed " (:branch config) by-hand)
                   reason (skipped id ": " (:branch config) " already holds these changes" by-hand)
                   :else (skipped id ": " (:branch config) " already holds these changes"))
         :proposed (cond
                     opened? (ok id ": opened " pull-request)
                     pushed? (ok id ": updated " pull-request)
                     :else (skipped id ": " pull-request " already holds these changes")))
       status))))

(defn- apply! [config repositories rules dir]
  (cond
    (nil? dir) (do (error "apply needs a working tree: krei apply DIR REPOSITORY") [:error])
    (not= 1 (count repositories)) (do (error "apply needs exactly one repository, got " (count repositories)) [:error])
    :else (each-repository
           repositories
           (fn [repository]
             (let [changes (plan/apply-into! config rules repository dir)]
               (if (empty? changes)
                 (ok (:id repository) " follows its rules")
                 (doseq [change changes]
                   (ok (:id repository) ": " (plan/describe change))))
               :applied)))))

(defn- list! [repositories]
  (doseq [{:keys [id default-branch]} repositories]
    (println (str id " " default-branch)))
  [])

(defn- rules! [rules]
  (doseq [{:keys [id doc]} rules]
    (println (str (name id) (when doc (str "  " doc)))))
  [])

(defn- unmatched
  "The `filters` that match none of `repositories`, each reported."
  [repositories filters]
  (for [f filters
        :when (not-any? #(inventory/matches? f %) repositories)]
    (do (error "no managed repository matches " f) :error)))

(def ^:private spec
  {:config {:desc "The config file"}
   :help {:coerce :boolean :alias :h}})

(defn parse
  "{:args :opts} of `argv`, refusing an option the spec does not name: an
  ignored --dry-run would run the real sync, and a mistyped --config the
  default config."
  [argv]
  (cli/parse-args argv {:spec spec :restrict true}))

(defn run
  "Runs the command `argv` names and returns the process exit code."
  [argv]
  (try
    (let [{:keys [args opts]} (parse argv)
          [command & filters] args
          [dir filters] (if (= "apply" command) [(first filters) (rest filters)] [nil filters])]
      (if (or (:help opts) (nil? command))
        (do (println usage) 0)
        (let [config (config/load-config (or (:config opts) (config/default-path)))
              rules (delay (rules/load-rules config))
              inventory (delay (inventory/read-inventory (:inventory config)))
              repositories (delay (inventory/select config @inventory filters))
              planned (delay (assoc config :calendar (calendar/calendar config (inventory/select config @inventory nil))))
              results (case command
                        "rules" (rules! @rules)
                        "list" (list! @repositories)
                        "plan" (plan! @planned @repositories @rules)
                        "check" (plan! @planned @repositories @rules)
                        "sync" (sync-all! @planned @repositories @rules)
                        "apply" (apply! @planned @repositories @rules dir)
                        (do (error "unknown command " command) (println usage) [:error]))
              results (concat (when (realized? repositories) (unmatched @repositories filters)) results)
              failed? (some #{:error} results)
              drifted? (and (= "check" command) (some #{:drift} results))]
          (if (or failed? drifted?) 1 0))))
    (catch clojure.lang.ExceptionInfo e
      (error (ex-message e))
      1)))

(defn -main [& argv]
  (System/exit (run argv)))
