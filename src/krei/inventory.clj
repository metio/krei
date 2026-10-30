;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.inventory
  "The repositories krei can manage, read from the inventory aro writes:
  a map from source id to repository records."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn])
  (:import (java.nio.file FileSystems)))

(defn id [{:keys [host path]}]
  (str host "/" path))

(defn read-inventory
  "Every repository record in the inventory at `path`, each with its :id."
  [path]
  (when-not (fs/exists? path)
    (throw (ex-info (str "no inventory at " path "; run aro fetch first") {:krei/problem :config})))
  (->> (edn/read-string (slurp (str path)))
       vals
       (apply concat)
       (map #(assoc % :id (id %)))
       (sort-by :id)
       (dedupe)))

(defn glob-matches?
  "Whether `pattern` matches the repository `id`. `*` stays within one path
  segment, `**` spans several."
  [pattern id]
  (.matches (.getPathMatcher (FileSystems/getDefault) (str "glob:" pattern)) (fs/path id)))

(defn matches?
  "Whether `pattern` matches the name or the id of `repository`."
  [pattern {:keys [name id]}]
  (or (glob-matches? pattern name) (glob-matches? pattern id)))

(defn- matches-any? [patterns repository]
  (some #(matches? % repository) patterns))

(defn select
  "The repositories of `inventory` the config manages, narrowed to those
  matching one of `filters` when any are given."
  [{:keys [include exclude archived forks]} inventory filters]
  (cond->> inventory
    (seq include) (filter #(matches-any? include %))
    (seq exclude) (remove #(matches-any? exclude %))
    (not archived) (remove :archived)
    (not forks) (remove :fork)
    (seq filters) (filter #(matches-any? filters %))))
