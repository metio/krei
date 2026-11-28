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

(deftest an-update-keeps-the-mode-of-the-file
  (let [rule {:id :r :files {"run.sh" {:replace [["old" "new"]]}}}
        dir (repo {"run.sh" "echo old\n"})
        script (fs/path dir "run.sh")]
    (fs/set-posix-file-permissions script "rwxr-xr-x")
    (rules/apply! dir (rules/changes [rule] dir {}))
    (is (= "echo new\n" (slurp (str script))))
    (is (= "rwxr-xr-x" (fs/posix->str (fs/posix-file-permissions script))))))

(deftest rule-dirs-take-precedence-and-hold-templates
  (fs/create-dirs (support/path "rules"))
  (spit (support/path "rules" "license.edn") (pr-str {:files {"NOTICE" {:template "notice.txt"}}}))
  (spit (support/path "rules" "notice.txt") "Copyright {{owner}}\n")
  (let [rule (rules/load-rule [(support/path "rules")] :license)
        dir (repo {})
        changes (rules/changes [rule] dir {:owner "The aro Authors"})]
    (is (= [[:create "NOTICE" :license]] (summary changes)))
    (rules/apply! dir changes)
    (is (= "Copyright The aro Authors\n" (slurp (str (fs/path dir "NOTICE")))))))

(deftest skip-when-and-unless-select-rules
  (let [rule {:id :readme :when [:name] :unless [:private] :files {"README.md" {:text "# {{name}}\n"}}}
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
    (is (= [[:create "README.md" :a]] (summary (rules/changes [a same] dir {}))))
    (testing "also when a third rule edits the file"
      (let [edit {:id :edit :files {"README.md" {:replace [["zzz" "q"]]}}}]
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"the rules a, b disagree about README.md"
                              (rules/changes [a b edit] dir {})))
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"the rules b, a disagree about README.md"
                              (rules/changes [b a edit] dir {})))
        (is (= [[:create "README.md" :edit]] (summary (rules/changes [a same edit] dir {}))))))))

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
      (is (not (fs/exists? (support/path "outside" "x.md"))))
      (testing "nothing is created behind the symlink before the refusal"
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"leaves the repository"
                              (rules/apply! dir [{:path "docs/new/x.md" :action :create :content (.getBytes "x")}])))
        (is (not (fs/exists? (support/path "outside" "new"))))))))

(deftest unknown-rules-and-file-specs-are-errors
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no rule nope" (rules/load-rule [] :nope)))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"unknown file"
                        (rules/changes [{:id :odd :files {"x" {:link "y"}}}] (repo {}) {}))))

(defn- rule-in [dir id rule files]
  (let [rules (support/path dir)]
    (fs/create-dirs rules)
    (spit (str (fs/path rules (str (name id) ".edn"))) (pr-str rule))
    (doseq [[file content] files]
      (fs/create-dirs (fs/parent (fs/path rules file)))
      (spit (str (fs/path rules file)) content))
    (rules/load-rule [(str rules)] id)))

(deftest absent-globs-delete-every-match
  (let [rule {:id :no-ide :files {".idea/**" :absent}}
        dir (repo {".idea/codeStyles/Project.xml" "x" ".idea/.name" "aro" "README.md" "hi"})]
    (is (= [[:delete ".idea/.name" :no-ide] [:delete ".idea/codeStyles/Project.xml" :no-ide]]
           (summary (rules/changes [rule] dir {}))))))

(deftest each-renders-the-files-once-per-element
  (let [rule (rule-in "rules" :texts {:each :licenses :files {"LICENSES/{{each}}.txt" {:verbatim "texts/{{each}}.txt" :optional true}}}
                      {"texts/0BSD.txt" "canonical zero" "texts/MIT.txt" "canonical mit"})
        dir (repo {"LICENSES/0BSD.txt" "zero" "LICENSES/MIT.txt" "canonical mit" "LICENSES/LicenseRef-X.txt" "x"})]
    (is (= [[:update "LICENSES/0BSD.txt" :texts]]
           (summary (rules/changes [rule] dir {:licenses ["0BSD" "LicenseRef-X" "MIT"]}))))))

(deftest copies-read-what-other-rules-write-in-the-same-run
  (let [texts {:id :texts :files {"LICENSES/0BSD.txt" {:text "canonical"}}}
        dir (repo {"LICENSES/0BSD.txt" "old" "LICENSE" "old"})
        changes (rules/changes [license texts] dir {:license "0BSD"})]
    (is (= [[:update "LICENSE" :license] [:update "LICENSES/0BSD.txt" :texts]] (summary changes)))
    (is (= "canonical" (String. ^bytes (:content (first changes)) "UTF-8")))))

(deftest replacements-edit-the-files-a-glob-selects
  (let [rule {:id :moved :files {".github/workflows/*.yml" {:replace [["metio/breto@[0-9a-f]+( # \\S+)?" "metio/ci/breto@{{ci.ref}} # {{ci.version}}"]]}}}
        dir (repo {".github/workflows/verify.yml" "- uses: metio/breto@abc123 # 2026.1.1\n"
                   ".github/workflows/release.yml" "- uses: metio/ci/breto@def456 # 2026.2.2\n"})
        changes (rules/changes [rule] dir {:ci {:ref "fed789" :version "2026.3.3"}})]
    (is (= [[:update ".github/workflows/verify.yml" :moved]] (summary changes)))
    (is (= "- uses: metio/ci/breto@fed789 # 2026.3.3\n" (String. ^bytes (:content (first changes)) "UTF-8")))))

(deftest replacements-skip-a-missing-file
  (let [rule {:id :moved :files {"action.yml" {:replace [["a" "b"]]}}}]
    (is (empty? (rules/changes [rule] (repo {"README.md" "a"}) {})))))

(deftest a-replacement-writes-facts-literally-and-expands-only-its-own-groups
  (let [rule {:id :r :files {"run.sh" {:replace [["x (n) y" "x {{v}} $1 \\$1 y"]]}}}
        content #(String. ^bytes (:content (first (rules/changes [rule] (repo {"run.sh" "x n y\n"}) {:v %}))) "UTF-8")]
    (is (= "x a$b n $1 y\n" (content "a$b")))
    (is (= "x $1 n $1 y\n" (content "$1")))
    (is (= "x a\\b n $1 y\n" (content "a\\b")))))

(deftest replacements-edit-what-other-rules-write-in-the-same-run
  (let [writer {:id :writer :files {"ci.yml" {:text "uses: old\nuses: old\n"}}}
        rule {:id :moved :files {"ci.yml" {:replace [["old" "new"]]}}}
        changes (rules/changes [writer rule] (repo {"ci.yml" "uses: older\n"}) {})]
    (is (= [[:update "ci.yml" :moved]] (summary changes)))
    (is (= "uses: new\nuses: new\n" (String. ^bytes (:content (first changes)) "UTF-8")))))

(deftest markdown-edits-compose-with-other-rules
  (let [header {:id :header :files {"README.md" {:markdown {:header "# {{title}}\n\n"}}}}
        license {:id :license :files {"README.md" {:markdown {:sections [{:heading "License" :body "[{{license}}](LICENSES/{{license}}.txt)." :place :end}]}}}}
        changes (rules/changes [header license] (repo {"README.md" "# old\n\n## License\n\nMIT\n\n## Usage\n\nRun it.\n"}) {:title "app" :license "0BSD"})]
    (is (= [[:update "README.md" :license]] (summary changes)))
    (is (= "# app\n\n## Usage\n\nRun it.\n\n## License\n\n[0BSD](LICENSES/0BSD.txt).\n" (String. ^bytes (:content (first changes)) "UTF-8")))
    (is (empty? (rules/changes [header license] (repo {"README.md" (String. ^bytes (:content (first changes)) "UTF-8")}) {:title "app" :license "0BSD"})))))

(deftest a-section-whose-body-renders-blank-is-removed
  (let [notes {:id :notes :files {"README.md" {:markdown {:sections [{:heading "Notes" :body "{% for n in notes %}- {{n}}\n{% endfor %}" :place :end}]}}}}]
    (is (= "Intro.\n\n## Notes\n\n- a\n"
           (String. ^bytes (:content (first (rules/changes [notes] (repo {"README.md" "Intro.\n"}) {:notes ["a"]}))) "UTF-8")))
    (is (= "Intro.\n"
           (String. ^bytes (:content (first (rules/changes [notes] (repo {"README.md" "Intro.\n\n## Notes\n\n- a\n"}) {:notes []}))) "UTF-8")))))

(deftest a-fact-set-to-false-is-unset
  (let [opted {:id :maven-verify :when [:maven-verify]}
        legacy {:id :no-legacy-java-ci :unless [:maven-verify]}]
    (is (false? (rules/applies? opted {:maven-verify false})))
    (is (true? (rules/applies? legacy {:maven-verify false})))
    (is (true? (rules/applies? opted {:maven-verify true})))
    (is (false? (rules/applies? legacy {:maven-verify true})))
    (is (true? (rules/applies? {:id :x :when [:n]} {:n 0})) "0 and other values that are not false count as set")))

(deftest a-copy-never-reads-through-a-symlink
  (spit (str (fs/create-dirs (support/path "outside")) "/secret.txt") "TOP SECRET")
  (testing "a symlinked file"
    (let [dir (repo {})]
      (fs/create-dirs (fs/path dir "LICENSES"))
      (fs/create-sym-link (fs/path dir "LICENSES" "MIT.txt") (support/path "outside" "secret.txt"))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no file LICENSES/MIT.txt"
                            (rules/changes [license] dir {:license "MIT"})))))
  (testing "a file under a symlinked directory"
    (let [dir (repo {})]
      (fs/delete-tree (fs/path dir "LICENSES"))
      (fs/create-sym-link (fs/path dir "LICENSES") (support/path "outside"))
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"no file LICENSES/secret.txt"
                            (rules/changes [license] dir {:license "secret"}))))))

(deftest a-skipped-optional-file-leaves-the-edit-of-its-path-alone
  (let [verbatim (rule-in "rules" :v {:files {"X" {:verbatim "missing.txt" :optional true}}} {})
        edit {:id :e :files {"X" {:replace [["old" "new"]]}}}
        dir (repo {"X" "old\n"})
        changes (rules/changes [verbatim edit] dir {})]
    (is (= [[:update "X" :e]] (summary changes)))
    (is (= "new\n" (String. ^bytes (:content (first changes)) "UTF-8")))))

(deftest an-edit-applies-to-a-file-another-rule-writes-in-the-same-run
  (let [write {:id :w :files {"README.md" {:text "# T\n\n## License\n\nMIT\n"}}}
        edit {:id :e :files {"README.md" {:markdown {:sections [{:heading "Mirrors" :body "- m" :place {:before "License"}}]}}}}
        dir (repo {})
        changes (rules/changes [write edit] dir {})]
    (is (= [[:create "README.md" :e]] (summary changes)))
    (is (= "# T\n\n## Mirrors\n\n- m\n\n## License\n\nMIT\n" (String. ^bytes (:content (first changes)) "UTF-8")))
    (testing "an edit of a file nobody writes or holds plans nothing"
      (is (= [] (rules/changes [edit] dir {}))))))

(deftest a-copy-reads-the-file-an-edit-leaves
  (let [edit {:id :e :files {"A" {:replace [["v1" "v2"]]}}}
        copy {:id :c :files {"B" {:copy "A"}}}
        dir (repo {"A" "v1\n"})
        changes (rules/changes [edit copy] dir {})]
    (is (= [[:update "A" :e] [:create "B" :c]] (summary changes)))
    (is (= ["v2\n" "v2\n"] (map #(String. ^bytes (:content %) "UTF-8") changes))))
  (testing "an edit of the copy applies to what the copy wrote"
    (let [copy {:id :c :files {"B" {:copy "A"}}}
          edit {:id :e :files {"B" {:replace [["v1" "v3"]]}}}
          dir (repo {"A" "v1\n"})]
      (is (= ["v3\n"] (map #(String. ^bytes (:content %) "UTF-8") (rules/changes [copy edit] dir {})))))))

(deftest an-edit-keeps-a-file-that-is-no-utf-8-as-it-is
  (let [dir (repo {})
        latin-1 (byte-array (map unchecked-byte [35 32 84 10 99 97 102 0xE9 10]))
        _ (fs/write-bytes (fs/path dir "README.md") latin-1)
        unchanged {:id :unchanged :files {"README.md" {:replace [["absent" "x"]]}}}
        changed {:id :changed :files {"README.md" {:replace [["T" "U"]]}}}]
    (testing "an edit that changes nothing leaves the bytes alone"
      (is (= [] (rules/changes [unchanged] dir {}))))
    (testing "an edit that changes the text refuses to re-encode the file"
      (is (thrown-with-msg? clojure.lang.ExceptionInfo #"README.md is no valid UTF-8"
                            (rules/changes [changed] dir {}))))))

(deftest a-rule-file-holds-one-map-within-the-bounds
  (fs/create-dirs (support/path "rules"))
  (let [refusal (fn [text]
                  (spit (support/path "rules" "r.edn") text)
                  (try (rules/load-rule [(support/path "rules")] :r) nil
                       (catch clojure.lang.ExceptionInfo e (:type (ex-data e)))))]
    (is (= :briko.edn/unexpected (refusal "[:files]")))
    (is (= :briko.edn/trailing-form (refusal "{} {}")))
    (is (= :briko.edn/tag-refused (refusal "{:files {\"a\" {:text #inst \"2026-01-01\"}}}")))
    (is (= :briko.edn/too-large (refusal (str "{:doc \"" (apply str (repeat rules/max-rule-bytes "x")) "\"}"))))))

(deftest every-shipped-rule-fits-the-bounds
  (doseq [file (fs/list-dir (clojure.java.io/file (clojure.java.io/resource "krei/rules")))
          :when (= "edn" (fs/extension file))]
    (is (map? (rules/load-rule [] (keyword (str (fs/strip-ext (fs/file-name file)))))) (str file))))

(deftest an-update-replaces-a-symlink-without-writing-through-it
  (let [outside (support/path "outside.txt")
        dir (repo {})
        link (fs/path dir "NOTICE")]
    (spit outside "kept\n")
    (fs/create-sym-link link outside)
    (rules/apply! dir [{:path "NOTICE" :action :update :content (.getBytes "new\n" "UTF-8")}])
    (is (not (fs/sym-link? link)))
    (is (= "new\n" (slurp (str link))))
    (is (= "kept\n" (slurp outside)))))

(deftest an-update-replaces-a-directory-and-leaves-no-temporary-file
  (let [dir (repo {"config/a.edn" "{}\n"})]
    (rules/apply! dir [{:path "config" :action :update :content (.getBytes "file\n" "UTF-8")}])
    (is (= "file\n" (slurp (str (fs/path dir "config")))))
    (is (= ["config"] (map (comp str fs/file-name) (fs/list-dir dir))))))
