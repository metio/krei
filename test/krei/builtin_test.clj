;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.builtin-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.facts :as facts]
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
