;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.calendar-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [krei.calendar :as calendar]))

(defn- repositories [ids]
  (map (fn [id] {:id id}) ids))

(defn- plan [facts]
  (calendar/calendar {:facts facts} (repositories (keys facts))))

(defn- field [cron n]
  (nth (str/split cron #" ") n))

(defn- ids [n]
  (map #(str "github.com/metio/repo-" %) (range n)))

(deftest cron-places-each-cadence-in-its-period
  (is (= "7 3 * * 1" (calendar/cron :weekly 0 7 3)))
  (is (= "7 3 * * 0" (calendar/cron :weekly 6 7 3)))
  (is (= "7 3 1 * *" (calendar/cron :monthly 0 7 3)))
  (is (= "7 3 28 * *" (calendar/cron :monthly 27 7 3)))
  (is (= "7 3 28 1,4,7,10 *" (calendar/cron :quarterly 27 7 3)))
  (is (= "7 3 1 3,6,9,12 *" (calendar/cron :quarterly 56 7 3))))

(deftest repositories-of-a-cadence-get-days-of-their-own
  (let [days (map #(field (:cron %) 2) (vals (plan (zipmap (ids 28) (repeat {:release {:cadence :monthly}})))))]
    (is (= 28 (count (set days)))))
  (testing "more repositories than days share them evenly"
    (let [days (map #(field (:cron %) 2) (vals (plan (zipmap (ids 30) (repeat {:release {:cadence :monthly}})))))]
      (is (= #{1 2} (set (vals (frequencies days))))))))

(deftest a-slot-holds-while-other-repositories-come-and-go
  (let [id "github.com/metio/reguloj"
        alone (get (plan {id {:release {:cadence :monthly}}}) id)
        others (plan (assoc (zipmap (ids 3) (repeat {:release {:cadence :weekly}})) id {:release {:cadence :monthly}}))]
    (is (= alone (get others id)))
    (is (= alone (get (plan {id {:release {:cadence :monthly}}}) id)))))

(deftest cadences-shape-the-cron
  (let [result (plan {"a" {:release {:cadence :weekly}}
                      "b" {:release {:cadence :quarterly}}
                      "c" {:release {:cadence :push}}})]
    (is (re-matches #"\d+ \d+ \* \* [0-6]" (get-in result ["a" :cron])))
    (is (re-matches #"\d+ \d+ \d+ (1,4,7,10|2,5,8,11|3,6,9,12) \*" (get-in result ["b" :cron])))
    (is (= {:cadence :push} (get result "c")))))

(deftest after-releases-follow-their-dependency
  (let [parent "github.com/metio/maven-parent"
        children (ids 3)
        result (plan (into {parent {:release {:cadence :monthly}}}
                           (for [id children] [id {:release {:cadence :monthly :after [parent]}}])))
        day #(parse-long (field (get-in result [% :cron]) 2))
        following (map #(inc (mod (+ (dec (day parent)) %) 28)) [1 2 3])]
    (is (= (set following) (set (map day children))))))

(deftest a-glob-gives-many-repositories-a-cadence
  (is (= #{"a" "b"} (set (keys (calendar/calendar {:facts {"*" {:release {:cadence :weekly}}}} (repositories ["a" "b"])))))))

(deftest broken-releases-are-errors
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"needs a :cadence" (plan {"a" {:release {:cadence :daily}}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"needs a :cadence" (plan {"a" {:release :monthly}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"which has no :release" (plan {"a" {:release {:cadence :monthly :after ["b"]}}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"whose :cadence differs"
                        (plan {"a" {:release {:cadence :monthly :after ["b"]}} "b" {:release {:cadence :weekly}}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"takes no :after"
                        (plan {"a" {:release {:cadence :push :after ["b"]}} "b" {:release {:cadence :push}}})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"form a cycle"
                        (plan {"a" {:release {:cadence :monthly :after ["b"]}} "b" {:release {:cadence :monthly :after ["a"]}}}))))
