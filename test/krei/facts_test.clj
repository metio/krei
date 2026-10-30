;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.facts :as facts]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(deftest detect-reads-the-only-license
  (fs/create-dirs (support/path "LICENSES"))
  (spit (support/path "LICENSES" "0BSD.txt") "text")
  (is (= {:license "0BSD"} (facts/detect support/*root*)))
  (testing "several licenses leave the fact unset"
    (spit (support/path "LICENSES" "MIT.txt") "text")
    (is (= {} (facts/detect support/*root*)))))

(deftest configured-facts-merge-from-least-to-most-specific
  (let [config {"*" {:owner "everyone" :level 0}
                "github.com/metio/*" {:level 2}
                "github.com/*/*" {:level 1 :host-wide true}
                "github.com/metio/aro" {:level 3}
                "codeberg.org/*/*" {:level 9}}]
    (is (= {:owner "everyone" :level 3 :host-wide true}
           (facts/configured config "github.com/metio/aro")))
    (is (= {:owner "everyone" :level 2 :host-wide true}
           (facts/configured config "github.com/metio/krei")))))

(deftest facts-prefer-config-over-detection-over-inventory
  (fs/create-dirs (support/path "LICENSES"))
  (spit (support/path "LICENSES" "0BSD.txt") "text")
  (let [repository {:id "github.com/metio/aro" :name "aro" :license "Apache-2.0"}]
    (is (= "0BSD" (:license (facts/facts {:facts {}} repository support/*root*))))
    (is (= "MIT" (:license (facts/facts {:facts {"github.com/metio/aro" {:license "MIT"}}} repository support/*root*))))))

(deftest render-fills-placeholders
  (is (= "LICENSES/0BSD.txt for aro" (facts/render "LICENSES/{license}.txt for {name}" {:license "0BSD" :name "aro"})))
  (is (= "kind: tool" (facts/render "kind: {kind}" {:kind :tool})))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no fact owner"
                        (facts/render "{owner}" {}))))
