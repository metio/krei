;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.config
  "The configuration: which repositories to manage, the rules they follow,
  and the facts those rules read.

  Every function takes an optional map {:getenv :getprop}, the environment
  briko.paths resolves the XDG directories and `~` against."
  (:require [babashka.fs :as fs]
            [briko.inventory :as inventory]
            [briko.paths :as paths]))

(def max-bytes
  "The largest configuration file krei reads, in UTF-8 bytes: 1 MiB, which
  briko.edn also takes by default. A configuration naming hundreds of
  repositories with their facts stays well below it."
  1048576)

(def max-depth
  "How deep the configuration may nest: briko.edn's default of 64. The
  facts of the README example nest five levels."
  64)

(defn default-path
  ([] (default-path nil))
  ([opts] (str (fs/path (paths/config-home opts) "krei" "config.edn"))))

(defn defaults
  ([] (defaults nil))
  ([opts]
   {:inventory (inventory/default-path opts)
    :include []
    :exclude []
    :archived false
    :forks false
    :rules [:license]
    :rule-dirs [(str (fs/path (paths/config-home opts) "krei" "rules"))]
    :facts {}
    :cache (str (fs/path (paths/cache-home opts) "krei"))
    :branch "krei/sync"
    :message "chore: align repository files with krei rules"
    :signoff true}))

(defn load-config
  "Reads the EDN config at `path` over the defaults. A missing file, a
  symbolic link to nothing and a file holding no form (empty, blank or only
  comments) all mean all defaults. Otherwise the file must hold one map
  within `max-bytes` and `max-depth`, and no tagged literal."
  ([path] (load-config path nil))
  ([path opts]
   (paths/load-edn-config path (defaults opts)
                          (merge opts
                                 {:edn {:max-bytes max-bytes :max-depth max-depth}
                                  :empty :defaults
                                  :dangling :missing
                                  :home-paths [[:inventory] [:cache] [:rule-dirs]]}))))
