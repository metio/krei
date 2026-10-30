;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts
  "What krei knows about one repository. Later sources override earlier
  ones: the inventory record, facts read from the checkout, then the config
  under \"*\", under glob patterns from the least to the most specific, and
  under the exact repository id."
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [krei.inventory :as inventory]))

(defn detect
  "Facts read from the checkout at `dir`. :license is the identifier of the
  only license in LICENSES/, and absent when there are several or none."
  [dir]
  (let [licenses (when (fs/directory? (fs/path dir "LICENSES"))
                   (fs/glob (fs/path dir "LICENSES") "*.txt"))]
    (cond-> {}
      (= 1 (count licenses)) (assoc :license (str (fs/strip-ext (fs/file-name (first licenses))))))))

(defn- specificity [pattern]
  (count (str/replace pattern #"[*?\[\]{}]" "")))

(defn configured
  "The facts the config `facts` holds for repository `id`, merged in order
  of specificity."
  [facts id]
  (let [patterns (->> (keys facts)
                      (remove #{"*" id})
                      (filter #(inventory/glob-matches? % id))
                      (sort-by (juxt specificity identity)))]
    (apply merge (get facts "*") (concat (map #(get facts %) patterns) [(get facts id)]))))

(defn facts
  "Every fact about `repository`, checked out at `dir`, under `config`."
  [config repository dir]
  (merge (dissoc repository :url) (detect dir) (configured (:facts config) (:id repository))))

(defn render
  "`template` with every {fact} replaced by that fact. A placeholder naming
  an unknown fact is an error, so a rule never writes a half-filled file."
  [template facts]
  (str/replace template #"\{([a-z][a-z0-9-]*)\}"
               (fn [[placeholder fact]]
                 (let [value (get facts (keyword fact))]
                   (when (nil? value)
                     (throw (ex-info (str "no fact " fact " for " placeholder) {:krei/problem :fact})))
                   (if (keyword? value) (name value) (str value))))))
