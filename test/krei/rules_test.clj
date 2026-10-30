;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.rules-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.rules :as rules]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(defn- repo [files]
  (let [dir (support/path "repo")]
    (fs/create-dirs dir)
    (doseq [[file content] files]
      (fs/create-dirs (fs/parent (fs/path dir file)))
      (spit (str (fs/path dir file)) content))
    dir))

(defn- summary [changes]
  (map (juxt :action :path :rule) changes))

(def license (rules/load-rule [] :license))

(deftest the-license-rule-copies-the-only-license
  (let [dir (repo {"LICENSES/0BSD.txt" "zero"})]
    (is (= [[:create "LICENSE" :license]] (summary (rules/changes [license] dir {:license "0BSD"}))))
    (spit (str (fs/path dir "LICENSE")) "stale")
    (is (= [[:update "LICENSE" :license]] (summary (rules/changes [license] dir {:license "0BSD"}))))
    (spit (str (fs/path dir "LICENSE")) "zero")
    (is (= [] (rules/changes [license] dir {:license "0BSD"})))
    (testing "without a license fact the rule does not apply"
      (is (= [] (rules/changes [license] dir {}))))))

(deftest absent-files-are-deleted
  (let [rule (rules/load-rule [] :no-golangci)
        dir (repo {".golangci.yml" "linters: {}"})]
    (is (= [[:delete ".golangci.yml" :no-golangci]] (summary (rules/changes [rule] dir {}))))
    (rules/apply! dir (rules/changes [rule] dir {}))
    (is (not (fs/exists? (fs/path dir ".golangci.yml"))))
    (is (= [] (rules/changes [rule] dir {})))))

(deftest rule-dirs-take-precedence-and-hold-templates
  (fs/create-dirs (support/path "rules"))
  (spit (support/path "rules" "license.edn") (pr-str {:files {"NOTICE" {:template "notice.txt"}}}))
  (spit (support/path "rules" "notice.txt") "Copyright {owner}\n")
  (let [rule (rules/load-rule [(support/path "rules")] :license)
        dir (repo {})
        changes (rules/changes [rule] dir {:owner "The aro Authors"})]
    (is (= [[:create "NOTICE" :license]] (summary changes)))
    (rules/apply! dir changes)
    (is (= "Copyright The aro Authors\n" (slurp (str (fs/path dir "NOTICE")))))))

(deftest skip-when-and-unless-select-rules
  (let [rule {:id :readme :when [:name] :unless [:private] :files {"README.md" {:text "# {name}\n"}}}
        dir (repo {})]
    (is (= 1 (count (rules/changes [rule] dir {:name "aro"}))))
    (is (= [] (rules/changes [rule] dir {:name "aro" :private true})))
    (is (= [] (rules/changes [rule] dir {:name "aro" :skip [:readme]})))
    (is (= [] (rules/changes [rule] dir {})))))

(deftest rules-must-agree-about-a-file
  (let [a {:id :a :files {"README.md" {:text "a"}}}
        b {:id :b :files {"README.md" {:text "b"}}}
        same {:id :same :files {"README.md" {:text "a"}}}
        dir (repo {})]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"disagree about README.md"
                          (rules/changes [a b] dir {})))
    (is (= [[:create "README.md" :a]] (summary (rules/changes [a same] dir {}))))))

(deftest paths-stay-inside-the-repository
  (let [dir (repo {})]
    (doseq [path ["../escape" "/etc/passwd" ".git/config" "a/../../b" ""]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"leaves the repository"
                            (rules/changes [{:id :bad :files {path {:text "x"}}}] dir {}))
          path))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"leaves the repository"
                          (rules/changes [{:id :bad :files {"LICENSE" {:copy "../secret"}}}] dir {}))))
  (testing "a symlinked directory pointing outside is refused on apply"
    (let [dir (repo {})]
      (fs/create-dirs (support/path "outside"))
      (fs/create-sym-link (fs/path dir "docs") (support/path "outside"))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"leaves the repository"
                            (rules/apply! dir (rules/changes [{:id :docs :files {"docs/x.md" {:text "x"}}}] dir {}))))
      (is (not (fs/exists? (support/path "outside" "x.md")))))))

(deftest unknown-rules-and-file-specs-are-errors
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no rule nope" (rules/load-rule [] :nope)))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unknown file"
                        (rules/changes [{:id :odd :files {"x" {:link "y"}}}] (repo {}) {}))))
