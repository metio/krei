;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.main
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
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
       (catch clojure.lang.ExceptionInfo e
         (error (:id repository) ": " (ex-message e))
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

(defn -main [& argv]
  (let [{:keys [args opts]} (cli/parse-args argv {:spec {:config {:desc "The config file"}
                                                         :help {:coerce :boolean :alias :h}}})
        [command & filters] args
        [dir filters] (if (= "apply" command) [(first filters) (rest filters)] [nil filters])]
    (if (or (:help opts) (nil? command))
      (println usage)
      (try
        (let [config (config/load-config (or (:config opts) (config/default-path)))
              rules (delay (rules/load-rules config))
              repositories (delay (inventory/select config (inventory/read-inventory (:inventory config)) filters))
              results (case command
                        "rules" (rules! @rules)
                        "list" (list! @repositories)
                        "plan" (plan! config @repositories @rules)
                        "check" (plan! config @repositories @rules)
                        "sync" (sync-all! config @repositories @rules)
                        "apply" (apply! config @repositories @rules dir)
                        (do (error "unknown command " command) (println usage) [:error]))
              results (concat (when (realized? repositories) (unmatched @repositories filters)) results)
              failed? (some #{:error} results)
              drifted? (and (= "check" command) (some #{:drift} results))]
          (System/exit (if (or failed? drifted?) 1 0)))
        (catch clojure.lang.ExceptionInfo e
          (error (ex-message e))
          (System/exit 1))))))
