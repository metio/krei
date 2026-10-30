;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.config
  "The configuration: which repositories to manage, the rules they follow,
  and the facts those rules read."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn home []
  (System/getProperty "user.home"))

(defn- env-dir [variable fallback]
  (let [value (System/getenv variable)]
    (if (str/blank? value) (str (fs/path (home) fallback)) value)))

(defn default-path []
  (str (fs/path (env-dir "XDG_CONFIG_HOME" ".config") "krei" "config.edn")))

(defn defaults []
  {:inventory (str (fs/path (env-dir "XDG_STATE_HOME" ".local/state") "aro" "inventory.edn"))
   :include []
   :exclude []
   :archived false
   :forks false
   :rules [:license]
   :rule-dirs [(str (fs/path (env-dir "XDG_CONFIG_HOME" ".config") "krei" "rules"))]
   :facts {}
   :cache (str (fs/path (env-dir "XDG_CACHE_HOME" ".cache") "krei"))
   :branch "krei/sync"
   :message "chore: align repository files with krei rules"
   :signoff true})

(defn expand-home [s]
  (some-> s (str/replace-first #"^~(?=/|$)" (home))))

(defn load-config
  "Reads the EDN config at `path` over the defaults. A missing file means all
  defaults."
  [path]
  (let [user (when (fs/exists? path) (edn/read-string (slurp (str path))))]
    (-> (merge (defaults) user)
        (update :inventory expand-home)
        (update :cache expand-home)
        (update :rule-dirs #(mapv expand-home %)))))
