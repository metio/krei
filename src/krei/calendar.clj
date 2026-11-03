;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.calendar
  "When each repository releases. The :release fact names a :cadence, and
  every repository of a scheduled cadence gets a slot of its own: a day of
  the week, of the month or of the quarter, at a minute and an hour. The slot
  comes from a hash of the repository id, so it holds while other
  repositories come and go. A slot another repository of the cadence holds
  passes on to the next free one, and a repository with :after takes the
  first free slot after those of the repositories it names."
  (:require [clojure.string :as str]
            [krei.facts :as facts])
  (:import (java.security MessageDigest)
           (java.util Arrays)))

(def ^:private periods
  "The slots of each scheduled cadence. Months count 28 days, so that every
  slot comes around in every month."
  {:weekly 7 :monthly 28 :quarterly 84})

(def cadences (conj (set (keys periods)) :push))

(defn- problem [message]
  (ex-info message {:krei/problem :fact}))

(defn- hashed
  "A number below `n` from the bytes `from` to `from`+8 of the SHA-256 of `id`."
  [id from n]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") (.getBytes ^String id "UTF-8"))]
    (.intValue (.mod (BigInteger. 1 (Arrays/copyOfRange digest (int from) (int (+ from 8))))
                     (BigInteger/valueOf n)))))

(defn cron
  "The cron expression of `slot` in `cadence`, at `minute` past `hour`.
  Weekly slots run from Monday to Sunday, and quarterly slots take the month
  of the quarter from the slot and the day from what remains."
  [cadence slot minute hour]
  (let [day (inc (rem slot 28))]
    (case cadence
      :weekly (str minute " " hour " * * " (mod (inc slot) 7))
      :monthly (str minute " " hour " " day " * *")
      :quarterly (let [month (inc (quot slot 28))]
                   (str minute " " hour " " day " " (str/join "," (range month 13 3)) " *")))))

(defn- releases
  "The :release fact of each of `repositories` that has one in `config`,
  checked."
  [config repositories]
  (let [found (into (sorted-map)
                    (for [{:keys [id]} repositories
                          :let [release (:release (facts/configured (:facts config) id))]
                          :when release]
                      [id release]))]
    (doseq [[id {:keys [cadence after] :as release}] found]
      (when-not (and (map? release) (cadences cadence))
        (throw (problem (str "the :release of " id " needs a :cadence of " (str/join ", " (sort (map name cadences)))))))
      (doseq [other after]
        (cond
          (= :push cadence) (throw (problem (str id " releases on every push, so its :release takes no :after")))
          (not (contains? found other)) (throw (problem (str id " releases after " other ", which has no :release")))
          (not= cadence (:cadence (found other))) (throw (problem (str id " releases after " other ", whose :cadence differs"))))))
    found))

(defn- in-order
  "The ids of `found` with every repository after those its :after names,
  otherwise in id order."
  [found]
  (loop [order [] left (keys found)]
    (if (empty? left)
      order
      (if-let [id (first (filter (fn [id] (every? (set order) (:after (found id)))) left))]
        (recur (conj order id) (remove #{id} left))
        (throw (problem (str "the :after of " (str/join ", " left) " form a cycle")))))))

(defn calendar
  "The release slot of every one of `repositories` whose :release in
  `config` names a scheduled cadence, as {id {:cadence :cron}}, and
  {id {:cadence :push}} for those that release on every push. A slot that
  is taken passes on to the next free one; when every slot of a cadence is
  taken, it passes on to the next one fewest repositories share."
  [config repositories]
  (let [found (releases config repositories)]
    (loop [[id & more] (in-order found) slots {} result {}]
      (if-not id
        result
        (let [{:keys [cadence after]} (found id)
              period (periods cadence)]
          (if-not period
            (recur more slots (assoc result id {:cadence cadence}))
            (let [start (if (seq after)
                          (inc (apply max (map slots after)))
                          (hashed id 0 period))
                  taken (frequencies (for [[other slot] slots :when (= cadence (:cadence (found other)))] slot))
                  slot (first (sort-by #(get taken % 0) (map #(mod (+ start %) period) (range period))))]
              (recur more
                     (assoc slots id slot)
                     (assoc result id {:cadence cadence
                                       :cron (cron cadence slot (hashed id 8 60) (hashed id 16 24))})))))))))
