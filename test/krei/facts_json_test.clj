;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts-json-test
  "The :json filter and yaml-scalar write the bytes cheshire's pretty
  printer and generate-string write for the same values. The reference
  calls cheshire directly, so a change of either side shows here."
  (:require [cheshire.core :as cheshire]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [krei.facts :as facts]))

(def ^:private trials 300)

(def ^:private odd-code-points
  "Code points a writer may escape or a reader may trip over: non-ASCII
  letters, hidden and bidirectional format characters, line and paragraph
  separators, the BOM, C0 and C1 controls, DEL, an astral emoji, a tag
  character and lone surrogates."
  (mapv #(String. (Character/toChars %))
        [0xe9 0x200b 0x200d 0x202e 0x2028 0x2029 0xfeff 0x00 0x07 0x1b 0x7f 0x85 0x9f
         0x1f600 0xe0041 0xd800 0xdfff]))

(defn- gen-text-of [code-points]
  (gen/fmap str/join
            (gen/vector (gen/frequency [[6 (gen/fmap str gen/char)]
                                        [2 (gen/elements code-points)]
                                        [1 (gen/elements ["\n" "\r\n" "\t" ": " "\"" "\\" "#" "- "])]])
                        0 12)))

(def ^:private gen-text (gen-text-of odd-code-points))

(def ^:private gen-yaml-text
  "Text without lone surrogates, which SnakeYAML refuses before yaml-scalar
  reaches the JSON writer."
  (gen-text-of (remove #(and (= 1 (count %)) (Character/isSurrogate (.charAt ^String % 0))) odd-code-points)))

(def ^:private gen-scalar
  (gen/one-of [gen-text gen/large-integer (gen/double* {:NaN? false :infinite? false})
               gen/boolean (gen/return nil) gen/keyword]))

(def ^:private gen-key
  (gen/one-of [gen/keyword gen-text]))

(def ^:private gen-fact
  (gen/recursive-gen (fn [inner]
                       (gen/one-of [(gen/vector inner 0 4)
                                    (gen/map gen-key inner {:max-elements 4})]))
                     gen-scalar))

(defn- cheshire-json
  "The :json filter's bytes as cheshire's pretty printer writes them for
  the value the filter receives."
  [x]
  (cheshire/generate-string (#'facts/sorted (#'facts/plain x))
                            {:pretty {:indentation 2 :object-field-value-separator ": " :indent-arrays? true}}))

(defn- cheshire-yaml-scalar [x]
  (let [s (str x)
        dumped (str/trimr (yaml/generate-string s))]
    (if (str/includes? dumped "\n") (cheshire/generate-string s) dumped)))

(defn- check [property]
  (let [result (tc/quick-check trials property)]
    (is (:pass? result) (pr-str (select-keys result [:shrunk :seed])))))

(deftest json-writes-cheshires-bytes-for-generated-facts
  ;; A fact sits under a key, since a nil {{r}} is a missing fact.
  (check (prop/for-all [fact gen-fact]
           (let [r {"v" fact}]
             (= (cheshire-json r) (facts/render "{{r|json}}" {:r r}))))))

(deftest yaml-scalar-writes-cheshires-bytes-for-generated-text
  (check (prop/for-all [s gen-yaml-text]
           (= (cheshire-yaml-scalar s) (facts/yaml-scalar s)))))

(deftest the-generators-reach-the-quoted-and-the-hidden-cases
  ;; Without these, the properties could pass on plain ASCII alone.
  (let [texts (gen/sample gen-yaml-text 500)]
    (is (some #(str/includes? (str/trimr (yaml/generate-string %)) "\n") texts))
    (is (some #(str/includes? % (String. (Character/toChars 0x200b))) texts))))

(deftest the-reference-tells-a-different-layout-apart
  ;; Negative control: cheshire's default pretty layout differs from the
  ;; filter's, so the property compares something.
  (let [fact {"a" [1 2] "b" {"c" "d"}}]
    (is (not= (cheshire/generate-string fact {:pretty true})
              (facts/render "{{r|json}}" {:r fact})))
    (is (= (cheshire-json fact) (facts/render "{{r|json}}" {:r fact})))))

(deftest json-collapses-keys-with-the-same-name
  ;; `sorted` orders keys by name, so :a and "a" become one key before
  ;; anything is written.
  (is (= "{\n  \"a\": 2\n}" (facts/render "{{r|json}}" {:r {:a 1 "a" 2}}))))

(deftest json-refuses-keys-that-print-to-the-same-name-as-a-fact-problem
  (let [e (try (facts/render "{{r|json}}" {:r {:ns/a 1 "ns/a" 2}}) nil
               (catch clojure.lang.ExceptionInfo e e))]
    ;; Selmer wraps what a filter throws and keeps it as the cause.
    (is (= :fact (:krei/problem (ex-data (ex-cause e)))))
    (is (str/includes? (ex-message e) "same JSON name"))))
