;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.git-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [krei.git :as git]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(deftest git-answers-exit-and-output
  (let [{:keys [exit out err]} (git/git support/*root* "--version")]
    (is (zero? exit))
    (is (str/starts-with? out "git version"))
    (is (= "" err)))
  (let [{:keys [exit out err]} (git/git support/*root* "rev-parse" "--git-dir")]
    (is (= 128 exit))
    (is (= "" out))
    (is (str/includes? err "not a git repository"))))

(deftest git!-trims-the-output-and-throws-with-the-error
  (let [{:keys [url]} (support/upstream! "aro" {"README.md" "x"})]
    (is (re-matches #"[0-9a-f]{40}" (git/git! url "rev-parse" "main")))
    (let [e (try (git/git! url "rev-parse" "--verify" "no-such-ref") nil
                 (catch clojure.lang.ExceptionInfo e e))]
      (is (= :git (:krei/problem (ex-data e))))
      (is (str/starts-with? (ex-message e) "git rev-parse --verify no-such-ref: ")))))

(deftest git-sees-the-environment-krei-adds
  (binding [git/*environment* (assoc git/*environment* "GIT_AUTHOR_NAME" "Ada")]
    (is (str/starts-with? (git/git! support/*root* "var" "GIT_AUTHOR_IDENT") "Ada "))))

(deftest git-reads-a-closed-stdin
  (is (= "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391"
         (git/git! support/*root* "hash-object" "--stdin"))
      "the hash of no bytes: git reads end of file at once instead of waiting"))

(deftest a-git-that-cannot-start-is-a-git-problem
  (let [missing (support/path "no-such-dir")]
    (is (= 127 (:exit (git/git missing "status"))))
    (let [e (try (git/git! missing "status") nil (catch clojure.lang.ExceptionInfo e e))]
      (is (= :git (:krei/problem (ex-data e))))
      (is (str/includes? (ex-message e) "could not run the program")))))
