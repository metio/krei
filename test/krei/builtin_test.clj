;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.builtin-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.facts :as facts]
            [krei.plan :as plan]
            [krei.rules :as rules]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(def seb {:given-names "Sebastian" :family-names "Hoß" :email "seb@hoß.de"
          :old-emails ["sebhoss@pm.me" "mail@shoss.de"]})

(def devcontainers
  (facts/derive-facts {:name "devcontainers" :host "github.com" :path "metio/devcontainers"
                       :license "0BSD" :authors [seb] :keywords ["make" "podman"]
                       :security-email "security@metio.wtf" :conduct-email "abuse@metio.wtf"}))

(defn- render [rule facts]
  (let [dir (support/path "repo")]
    (fs/create-dirs dir)
    (into {} (for [{:keys [path content]} (rules/changes [(rules/load-rule [] rule)] dir facts)]
               [path (String. ^bytes content "UTF-8")]))))

(deftest citation-names-title-authors-homepage-license-and-keywords
  (is (= {"CITATION.cff"
          (str "# SPDX-FileCopyrightText: The devcontainers Authors\n"
               "# SPDX-License-Identifier: 0BSD\n"
               "\n"
               "cff-version: 1.2.0\n"
               "title: devcontainers\n"
               "message: If you use this software, please cite it as below.\n"
               "type: software\n"
               "authors:\n"
               "- family-names: Hoß\n"
               "  given-names: Sebastian\n"
               "  email: seb@hoß.de\n"
               "url: https://github.com/metio/devcontainers/\n"
               "license: 0BSD\n"
               "keywords:\n"
               "  - make\n"
               "  - podman\n")}
         (render :citation devcontainers)))
  (testing "no keywords, no keywords section; no email, no email line"
    (let [cff (get (render :citation (assoc devcontainers :keywords [] :authors [(dissoc seb :email)])) "CITATION.cff")]
      (is (str/ends-with? cff "  given-names: Sebastian\nurl: https://github.com/metio/devcontainers/\nlicense: 0BSD\n")))))

(deftest authors-lists-each-role-that-has-someone
  (is (= {"AUTHORS.md"
          (str "<!--\n"
               "SPDX-FileCopyrightText: The devcontainers Authors\n"
               "SPDX-License-Identifier: 0BSD\n"
               "-->\n"
               "\n"
               "# Authors\n"
               "\n"
               "This file lists all authors of this project. You can add yourself to this list\nonce you contributed a patch to the project.\n"
               "\n"
               "## Owners\n"
               "\n"
               "Owners have administrator access to the project.\n"
               "\n"
               "- Sebastian Hoß <seb@hoß.de>\n")}
         (render :authors devcontainers)))
  (let [md (get (render :authors (facts/derive-facts (-> devcontainers
                                                          (dissoc :owners :maintainers :contributors :translators)
                                                          (assoc :authors [seb {:given-names "Ann" :family-names "Lee" :role :translator}]))))
                "AUTHORS.md")]
    (is (str/ends-with? md "- Sebastian Hoß <seb@hoß.de>\n\n## Translators\n\nTranslators translate user facing text of the project.\n\n- Ann Lee\n"))))

(deftest mailmap-maps-old-addresses-to-the-current-one
  (is (= {".mailmap"
          (str "# SPDX-FileCopyrightText: The devcontainers Authors\n"
               "# SPDX-License-Identifier: 0BSD\n"
               "\n"
               "Sebastian Hoß <seb@hoß.de>\n"
               "Sebastian Hoß <seb@hoß.de> <sebhoss@pm.me>\n"
               "Sebastian Hoß <seb@hoß.de> <mail@shoss.de>\n")}
         (render :mailmap devcontainers))))

(deftest security-and-conduct-name-their-addresses
  (is (str/includes? (get (render :security devcontainers) "SECURITY.md") "Email <security@metio.wtf> with details"))
  (is (str/includes? (get (render :code-of-conduct devcontainers) "CODE_OF_CONDUCT.md") "enforcement at\n<abuse@metio.wtf>."))
  (is (= {} (render :security (dissoc devcontainers :security-email)))))

(deftest contributing-sets-up-the-tools-with-breto
  (let [text (get (render :contributing devcontainers) "CONTRIBUTING.md")]
    (is (str/includes? text "eval \"$(breto env)\""))
    (is (not (str/includes? text "nix develop")))))

(deftest static-files-carry-the-repository-header
  (doseq [rule [:editorconfig :gitattributes :contributing]
          [path text] (render rule devcontainers)]
    (is (str/includes? text "SPDX-FileCopyrightText: The devcontainers Authors\n") path)
    (is (str/includes? text "SPDX-License-Identifier: 0BSD\n") path)
    (is (not (str/includes? text "{{")) path)))

(deftest go-sum-license-only-in-go-modules
  (is (= {} (render :go-sum-license devcontainers)))
  (is (= {"go.sum.license" "# SPDX-FileCopyrightText: The devcontainers Authors\n# SPDX-License-Identifier: 0BSD\n"}
         (render :go-sum-license (assoc devcontainers :go true)))))

(def health-config
  {"github.com/metio/*" {:health-files-from "github.com/metio/.github"}
   "github.com/metio/.github" {:health-files-from nil :copyright "The metio Authors"}})

(defn- health-changes [id]
  (let [dir (support/path id)
        facts (merge devcontainers (facts/configured health-config id))
        health [:security :code-of-conduct :contributing :no-health-files]]
    (fs/create-dirs dir)
    (doseq [file ["SECURITY.md" "CODE_OF_CONDUCT.md" "CONTRIBUTING.md"]]
      (spit (str (fs/path dir file)) "local copy\n"))
    (sort (for [{:keys [action path rule]} (rules/changes (mapv #(rules/load-rule [] %) health) dir facts)]
            [action path rule]))))

(deftest health-files-come-from-the-org-repository
  (testing "repositories of the organization drop their own copies"
    (is (= [[:delete "CODE_OF_CONDUCT.md" :no-health-files]
            [:delete "CONTRIBUTING.md" :no-health-files]
            [:delete "SECURITY.md" :no-health-files]]
           (health-changes "github.com/metio/reguloj"))))
  (testing "the org repository keeps the files the others fall back to"
    (is (= #{:code-of-conduct :contributing :security}
           (set (map last (health-changes "github.com/metio/.github")))))))

(deftest license-texts-and-license-follow-the-spdx-reference
  (let [dir (support/path "repo")
        spdx (slurp (clojure.java.io/resource "krei/rules/license-texts/0BSD.txt"))]
    (fs/create-dirs (fs/path dir "LICENSES"))
    (spit (str (fs/path dir "LICENSES" "0BSD.txt")) "wrapped variant")
    (spit (str (fs/path dir "LICENSES" "LicenseRef-X.txt")) "own text")
    (let [changes (rules/changes (mapv #(rules/load-rule [] %) [:license :license-texts]) dir
                                 (assoc devcontainers :licenses ["0BSD" "LicenseRef-X"]))]
      (is (= [["LICENSE" spdx] ["LICENSES/0BSD.txt" spdx]]
             (map (juxt :path #(String. ^bytes (:content %) "UTF-8")) changes))))))

(deftest renovate-extends-the-shared-preset-and-keeps-additions
  (let [facts (assoc devcontainers :renovate {"$schema" "https://docs.renovatebot.com/renovate-schema.json"
                                              "extends" ["github>metio/renovate-config"]
                                              "git-submodules" {"enabled" true}})]
    (is (= (str "// SPDX-FileCopyrightText: The devcontainers Authors\n"
                "// SPDX-License-Identifier: 0BSD\n"
                "{\n"
                "  \"$schema\": \"https://docs.renovatebot.com/renovate-schema.json\",\n"
                "  \"extends\": [\n"
                "    \"github>metio/renovate-config\"\n"
                "  ],\n"
                "  \"git-submodules\": {\n"
                "    \"enabled\": true\n"
                "  }\n"
                "}\n")
           (get (render :renovate facts) "renovate.json5")))))

(def java-library
  (merge devcontainers {:default-branch "main" :maven true :breto true
                        :ci {:ref "abc123" :version "2026.11.1"}
                        :maven-central {:day 3 :paths "src/main/java pom.xml"}}))

(deftest maven-central-release-calls-the-shared-workflow-on-its-day
  (let [release (get (render :maven-central-release java-library) ".github/workflows/release.yml")]
    (is (str/includes? release "    - cron: 23 4 3 * *\n"))
    (is (str/includes? release "  group: ${{ github.workflow }}\n"))
    (is (str/includes? release "maven-central-release.yml@abc123 # 2026.11.1\n    with:\n      paths: src/main/java pom.xml\n    secrets: inherit\n"))
    (testing "deploy-args only when set"
      (is (not (str/includes? release "deploy-args")))
      (is (str/includes? (get (render :maven-central-release (assoc-in java-library [:maven-central :deploy-args] "--projects a"))
                              ".github/workflows/release.yml")
                         "      deploy-args: --projects a\n    secrets: inherit\n")))
    (testing "needs the opt-in fact"
      (is (empty? (render :maven-central-release (dissoc java-library :maven-central)))))))

(deftest maven-verify-passes-inputs-only-when-set
  (let [verify #(get (render :maven-verify (assoc java-library :maven-verify %)) ".github/workflows/verify.yml")]
    (is (str/includes? (verify {}) "maven.yml@abc123 # 2026.11.1\n  reuse:\n"))
    (is (str/includes? (verify {:goals "--activate-profiles=native verify"})
                       "    with:\n      goals: --activate-profiles=native verify\n  reuse:\n"))
    (is (str/includes? (verify {}) "    branches: [main]\n"))
    (is (str/includes? (verify {}) "          NEEDS: ${{ toJSON(needs) }}\n"))))

(deftest no-legacy-java-ci-only-once-maven-verify-runs
  (let [dir (support/path "repo")]
    (doseq [f ["java.properties" ".github/actions/managed-java/action.yml" ".github/workflows/reuse.yml"]]
      (fs/create-dirs (fs/parent (fs/path dir f)))
      (spit (str (fs/path dir f)) "x"))
    (is (= #{"java.properties" ".github/actions/managed-java/action.yml" ".github/workflows/reuse.yml"}
           (set (map :path (rules/changes [(rules/load-rule [] :no-legacy-java-ci)] dir (assoc java-library :maven-verify {}))))))
    (is (empty? (rules/changes [(rules/load-rule [] :no-legacy-java-ci)] dir java-library)))))

(deftest workflow-rules-keep-the-pins-the-repository-has
  (let [dir (support/path "repo")
        file (str dir "/.github/workflows/verify.yml")
        fresh (get (render :maven-verify (assoc java-library :maven-verify {})) ".github/workflows/verify.yml")
        bumped (-> fresh
                   (str/replace #"actions/checkout@[0-9a-f]{40} # v7" "actions/checkout@1111111111111111111111111111111111111111 # v8"))]
    (testing "a new file carries the template's pins"
      (is (str/includes? fresh "actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1 # v7")))
    (fs/create-dirs (fs/parent file))
    (spit file bumped)
    (testing "an existing file keeps its own pins, so Renovate's bumps are no drift"
      (is (empty? (rules/changes [(rules/load-rule [] :maven-verify)] dir (assoc java-library :maven-verify {})))))
    (testing "everything else still follows the template"
      (spit file (str/replace bumped "timeout-minutes: 5" "timeout-minutes: 9"))
      (let [[change] (rules/changes [(rules/load-rule [] :maven-verify)] dir (assoc java-library :maven-verify {}))
            text (String. ^bytes (:content change) "UTF-8")]
        (is (= :update (:action change)))
        (is (str/includes? text "timeout-minutes: 5"))
        (is (str/includes? text "actions/checkout@1111111111111111111111111111111111111111 # v8"))))))

(deftest reuse-workflow-replaces-an-old-action-and-keeps-pins
  (let [dir (support/path "repo")
        file (str dir "/.github/workflows/reuse.yml")
        facts (assoc devcontainers :default-branch "main" :reuse-workflow true)
        changes #(rules/changes [(rules/load-rule [] :reuse-workflow)] dir %)]
    (fs/create-dirs (fs/parent file))
    (spit file "jobs:\n  reuse:\n    steps:\n      - uses: fsfe/reuse-action@v2\n")
    (let [[change] (changes facts)
          text (String. ^bytes (:content change) "UTF-8")]
      (is (= :update (:action change)))
      (is (str/includes? text "fsfe/reuse-action@676e2d560c9a403aa252096d99fcab3e1132b0f5 # v6\n"))
      (is (str/includes? text "    branches: [main]\n"))
      (spit file text))
    (spit file (str/replace (slurp file) #"reuse-action@[0-9a-f]{40} # v6" "reuse-action@2222222222222222222222222222222222222222 # v7"))
    (is (empty? (changes facts)))
    (is (empty? (changes (assoc facts :maven-verify {}))))))

(deftest pinned-reuse-action-reports-tags
  (let [dir (support/path "repo")
        rule [(rules/load-rule [] :pinned-reuse-action)]
        file (str dir "/.github/workflows/verify.yml")]
    (fs/create-dirs (fs/parent file))
    (spit file "      - uses: fsfe/reuse-action@676e2d560c9a403aa252096d99fcab3e1132b0f5 # v6\n")
    (is (empty? (rules/violations rule dir devcontainers)))
    (spit file "      - uses: fsfe/reuse-action@v5\n")
    (is (= [".github/workflows/verify.yml"] (map :path (rules/violations rule dir devcontainers))))))

(deftest checks-read-the-files-as-the-changes-leave-them
  (let [dir (support/path "repo")
        file (str dir "/.github/workflows/reuse.yml")
        facts (assoc devcontainers :default-branch "main" :reuse-workflow true)
        rules [(rules/load-rule [] :reuse-workflow) (rules/load-rule [] :pinned-reuse-action)]]
    (fs/create-dirs (fs/parent file))
    (spit file "      - uses: fsfe/reuse-action@v2\n")
    (testing "the tree as it stands breaks the check"
      (is (= 1 (count (rules/violations rules dir facts)))))
    (testing "the change that pins the action fixes it in the same run"
      (is (empty? (rules/violations rules dir facts (rules/changes rules dir facts)))))
    (testing "a file the changes create is checked too"
      (is (= [".github/workflows/new.yml"]
             (map :path (rules/violations rules dir facts
                                          (conj (rules/changes rules dir facts)
                                                {:rule :x :path ".github/workflows/new.yml" :action :create
                                                 :content (.getBytes "uses: fsfe/reuse-action@v5\n" "UTF-8")}))))))
    (testing "a file the changes delete is not"
      (is (empty? (rules/violations rules dir facts [{:rule :x :path ".github/workflows/reuse.yml" :action :delete}]))))))

(deftest shared-toolchains-reports-own-pins
  (let [dir (support/path "repo")
        rule [(rules/load-rule [] :shared-toolchains)]
        file (str dir "/.breto.edn")
        facts (assoc devcontainers :breto true)]
    (fs/create-dirs dir)
    (spit file "{:packages [\"@go\" \"@lint\" \"jq\"]}\n")
    (is (empty? (rules/violations rule dir facts)))
    (spit file "{:packages [\"@go\" \"helm@3.19.0\"]}\n")
    (is (= [".breto.edn"] (map :path (rules/violations rule dir facts))))
    (testing "a package in :breto-pins may carry its own version, the others may not"
      (is (empty? (rules/violations rule dir (assoc facts :breto-pins ["helm"]))))
      (spit file "{:packages [\"helm@3.19.0\" \"kind@0.31.0\"]}\n")
      (is (= [".breto.edn:1 matches \"([A-Za-z0-9._+-]+)@[0-9][^\"]*\" (shared-toolchains)"]
             (map plan/violation (rules/violations rule dir (assoc facts :breto-pins ["helm"])))))
      (spit file "{:packages [\"kind@0.31.0\" \"helm@3.19.0\"]}\n")
      (is (= 1 (count (rules/violations rule dir (assoc facts :breto-pins ["helm"]))))))))

(deftest main-branch-reports-master-without-touching-files
  (let [dir (support/path "repo")
        rule [(rules/load-rule [] :main-branch)]
        write (fn [path text]
                (fs/create-dirs (fs/parent (fs/path dir path)))
                (spit (str (fs/path dir path)) text))]
    (fs/create-dirs dir)
    (is (empty? (rules/violations rule dir (assoc devcontainers :default-branch "main"))))
    (is (= ["default-branch is \"master\", expected \"main\" (main-branch)"]
           (map plan/violation (rules/violations rule dir (assoc devcontainers :default-branch "master")))))
    (write ".github/workflows/verify.yml" "on:\n  pull_request:\n    branches: [master]\n")
    (write ".github/workflows/release.yml" "on:\n  push:\n    branches:\n      - main\n      - master\n")
    (write "README.md" "See https://github.com/metio/devcontainers/blob/master/README.md\n")
    (write "docs/guide.md" "Uses https://github.com/metio/devcontainers/tree/master/docs\n")
    (write ".github/workflows/publish.yml" "      - uses: wjdp/htmltest-action@master\n")
    (is (= [".github/workflows/release.yml" ".github/workflows/verify.yml" "README.md" "docs/guide.md"]
           (sort (distinct (map :path (rules/violations rule dir (assoc devcontainers :default-branch "main"))))))
        "a third-party action on its master branch is fine")
    (is (empty? (rules/changes rule dir (assoc devcontainers :default-branch "master"))))))
