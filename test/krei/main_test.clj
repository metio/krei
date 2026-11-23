;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.main-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [krei.main :as main]))

(deftest unknown-options-are-refused
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"dry-run" (main/parse ["sync" "--dry-run"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"cfg" (main/parse ["--cfg" "x.edn" "sync"])))
  (binding [*out* (java.io.StringWriter.) *err* (java.io.StringWriter.)]
    (is (= 1 (main/run ["--cfg" "x.edn" "list"])) "nothing runs on the default config")
    (is (= 0 (main/run ["--help"])))))

(deftest known-options-are-read
  (is (= {:args ["sync"] :opts {:config "k.edn"}} (main/parse ["--config" "k.edn" "sync"]))))

(deftest one-failing-repository-does-not-stop-the-others
  (let [err (java.io.StringWriter.)
        results (binding [*err* err]
                  (#'krei.main/each-repository
                   [{:id "a"} {:id "b"} {:id "c"} {:id "d"}]
                   (fn [{:keys [id]}]
                     (case id
                       "b" (throw (RuntimeException. "EOF while reading"))
                       "d" (throw (NullPointerException.))
                       :in-sync))))]
    (is (= [:in-sync :error :in-sync :error] results))
    (is (str/includes? (str err) "[ERROR] b: EOF while reading"))
    (is (str/includes? (str err) "[ERROR] d: java.lang.NullPointerException"))))
