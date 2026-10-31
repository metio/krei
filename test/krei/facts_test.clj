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
  (is (= {:license "0BSD" :licenses ["0BSD"]} (facts/detect support/*root*)))
  (testing "several licenses leave the fact unset"
    (spit (support/path "LICENSES" "MIT.txt") "text")
    (is (= {:licenses ["0BSD" "MIT"]} (facts/detect support/*root*)))))

(deftest detect-matches-marker-globs
  (fs/create-dirs (support/path "charts" "app"))
  (spit (support/path "charts" "app" "Chart.yaml") "name: app")
  (spit (support/path ".breto.edn") "{}")
  (is (= {:helm true :breto true} (facts/detect support/*root*))))

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

(deftest configured-maps-merge-key-by-key
  (is (= {:renovate {"extends" ["base"] "packageRules" [1]} :skip [:b]}
         (facts/configured {"*" {:renovate {"extends" ["base"]} :skip [:a]}
                            "github.com/metio/aro" {:renovate {"packageRules" [1]} :skip [:b]}}
                           "github.com/metio/aro"))))

(deftest json-prints-facts-with-sorted-keys
  (is (= "{\n  \"$schema\": \"s\",\n  \"extends\": [\n    \"a\"\n  ]\n}"
         (facts/render "{{r|json}}" {:r {"extends" ["a"] "$schema" "s"}}))))

(deftest facts-prefer-config-over-detection-over-inventory
  (fs/create-dirs (support/path "LICENSES"))
  (spit (support/path "LICENSES" "0BSD.txt") "text")
  (let [repository {:id "github.com/metio/aro" :name "aro" :license "Apache-2.0"}]
    (is (= "0BSD" (:license (facts/facts {:facts {}} repository support/*root*))))
    (is (= "MIT" (:license (facts/facts {:facts {"github.com/metio/aro" {:license "MIT"}}} repository support/*root*))))))

(deftest render-fills-in-selmer-templates
  (is (= "LICENSES/0BSD.txt for aro" (facts/render "LICENSES/{{license}}.txt for {{name}}" {:license "0BSD" :name "aro"})))
  (is (= "kind: tool, tags: a b" (facts/render "kind: {{kind}}, tags:{% for t in tags %} {{t}}{% endfor %}" {:kind :tool :tags [:a :b]})))
  (is (= "a & <b>" (facts/render "{{x}}" {:x "a & <b>"})))
  (testing "an unknown value is an error, an unknown condition or loop is empty"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no fact owner"
                          (facts/render "{{owner}}" {})))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no fact a.email"
                          (facts/render "{{a.email}}" {:a {}})))
    (is (= "[]" (facts/render "[{% if owner %}x{% endif %}{% for k in keywords %}{{k}}{% endfor %}]" {})))))

(deftest detect-names-the-build-tools-present
  (spit (support/path "go.mod") "module x")
  (spit (support/path "bb.edn") "{}")
  (is (= {:go true :babashka true} (facts/detect support/*root*))))

(deftest derived-facts-fill-in-what-no-source-sets
  (let [seb {:given-names "Sebastian" :family-names "Hoß"}
        ann {:given-names "Ann" :family-names "Lee" :role :translator}
        derived (facts/derive-facts {:name "aro" :host "github.com" :path "metio/aro" :topics ["git"] :authors [seb ann]})]
    (is (= "aro" (:title derived)))
    (is (= "The aro Authors" (:copyright derived)))
    (is (= "https://github.com/metio/aro/" (:homepage derived)))
    (is (= ["git"] (:keywords derived)))
    (is (= [seb] (:owners derived)))
    (is (= [ann] (:translators derived)))
    (is (= [] (:maintainers derived))))
  (testing "set facts win"
    (let [derived (facts/derive-facts {:name "yosql" :title "YoSQL" :homepage "https://yosql.example/" :keywords []})]
      (is (= "The YoSQL Authors" (:copyright derived)))
      (is (= "https://yosql.example/" (:homepage derived)))
      (is (= [] (:keywords derived))))))

(deftest the-repository-states-its-own-facts-last
  (spit (support/path ".krei.edn") "{:typos {:words [\"edn\"]} :title \"Aro\"}")
  (let [config {:facts {"*" {:typos {:exclude ["LICENSES/"]} :title "aro"}}}
        facts (facts/facts config {:id "github.com/metio/aro" :name "aro"} support/*root*)]
    (is (= {:words ["edn"] :exclude ["LICENSES/"]} (:typos facts)))
    (is (= "Aro" (:title facts)))))
