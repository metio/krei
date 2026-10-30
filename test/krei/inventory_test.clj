;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.inventory-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [krei.inventory :as inventory]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(defn- record [path & {:as more}]
  (merge {:host "github.com" :path path :name (last (re-seq #"[^/]+" path)) :default-branch "main" :archived false :fork false}
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
