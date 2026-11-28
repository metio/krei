;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.inventory-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [krei.inventory :as inventory]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(defn- record [path & {:as more}]
  (merge {:host "github.com" :path path :name (last (re-seq #"[^/]+" path)) :namespace (first (re-seq #"[^/]+" path))
          :default-branch "main" :archived false :fork false :topics []}
         more))

(deftest read-inventory-flattens-the-sources
  (spit (support/path "inventory.edn")
        (str ";; generated\n"
             (pr-str {"github" [(record "metio/aro") (record "metio/krei")]
                      "metio" [(record "metio/aro")]})))
  (is (= ["github.com/metio/aro" "github.com/metio/krei"]
         (map :id (inventory/read-inventory (support/path "inventory.edn"))))))

(deftest missing-inventory-is-an-error
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"aro fetch"
                        (inventory/read-inventory (support/path "missing.edn")))))

(deftest missing-inventory-keeps-the-hint-and-the-cause
  (let [e (try (inventory/read-inventory (support/path "missing.edn")) (catch clojure.lang.ExceptionInfo e e))]
    (is (= :config (:krei/problem (ex-data e))))
    (is (= :briko.inventory/missing (:type (ex-data (ex-cause e)))))))

(deftest a-malformed-record-is-refused-not-dropped
  (spit (support/path "inventory.edn")
        (pr-str {"github" [(record "metio/aro") (dissoc (record "metio/krei") :topics)]}))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"lacks the keys \[:topics\]"
                        (inventory/read-inventory (support/path "inventory.edn")))))

(deftest an-inventory-holds-no-tagged-literal
  (spit (support/path "inventory.edn") "{\"github\" [#inst \"2026-01-01\"]}")
  (is (= :briko.edn/tag-refused
         (:type (ex-data (try (inventory/read-inventory (support/path "inventory.edn"))
                              (catch clojure.lang.ExceptionInfo e e)))))))

(deftest an-inventory-above-the-byte-limit-is-refused
  (spit (support/path "inventory.edn") (str "{\"github\" [" (apply str (repeat 8388608 " ")) "]}"))
  (is (= :briko.inventory/too-large
         (:type (ex-data (try (inventory/read-inventory (support/path "inventory.edn"))
                              (catch clojure.lang.ExceptionInfo e e)))))))

(deftest glob-matches-within-and-across-segments
  (is (inventory/glob-matches? "github.com/metio/*" "github.com/metio/aro"))
  (is (not (inventory/glob-matches? "github.com/*" "github.com/metio/aro")))
  (is (inventory/glob-matches? "github.com/**" "github.com/metio/aro")))

(deftest select-applies-include-exclude-archived-forks-and-filters
  (let [repositories (map #(assoc % :id (inventory/id %))
                          [(record "metio/aro")
                           (record "metio/krei")
                           (record "metio/old" :archived true)
                           (record "metio/copy" :fork true)
                           (record "sebhoss/dotfiles")])
        config {:include ["github.com/metio/*"] :exclude ["krei"] :archived false :forks false}]
    (is (= ["github.com/metio/aro"] (map :id (inventory/select config repositories []))))
    (is (= ["github.com/metio/aro" "github.com/metio/old"]
           (map :id (inventory/select (assoc config :archived true) repositories []))))
    (is (= [] (map :id (inventory/select config repositories ["dotfiles"]))))
    (is (= ["github.com/metio/aro" "github.com/metio/krei"]
           (map :id (inventory/select {} repositories ["k*" "a*"]))))
    (is (= ["github.com/sebhoss/dotfiles"]
           (map :id (inventory/select {} repositories ["dotfiles"]))))))
