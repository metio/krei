;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.main-test
  (:require [clojure.test :refer [deftest is]]
            [krei.main :as main]))

(deftest unknown-options-are-refused
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"dry-run" (main/parse ["sync" "--dry-run"])))
  (is (thrown-with-msg? clojure.lang.ExceptionInfo #"cfg" (main/parse ["--cfg" "x.edn" "sync"])))
  (binding [*out* (java.io.StringWriter.) *err* (java.io.StringWriter.)]
    (is (= 1 (main/run ["--cfg" "x.edn" "list"])) "nothing runs on the default config")
    (is (= 0 (main/run ["--help"])))))

(deftest known-options-are-read
  (is (= {:args ["sync"] :opts {:config "k.edn"}} (main/parse ["--config" "k.edn" "sync"]))))
