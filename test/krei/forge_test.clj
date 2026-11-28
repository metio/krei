;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.forge-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is use-fixtures]]
            [krei.forge :as forge]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(def ^:private repository
  {:host "github.com" :path "metio/aro" :default-branch "main"})

(defn- stub!
  "A gh stand-in that appends its arguments, one per line, to calls.log,
  and then runs `script`."
  [script]
  (let [p (support/path "gh")]
    (spit p (str "#!/bin/sh\nprintf '%s\\n' \"$@\" >> " (support/path "calls.log") "\necho -- >> "
                 (support/path "calls.log") "\n" script "\n"))
    (fs/set-posix-file-permissions p "rwx------")
    p))

(defn- calls []
  (let [log (support/path "calls.log")]
    (if (fs/exists? log)
      (->> (str/split (slurp log) #"(?m)^--\n") (mapv str/split-lines))
      [])))

(deftest an-open-pull-request-is-reused
  (binding [forge/*program* (stub! "echo https://github.com/metio/aro/pull/7")]
    (is (= ["https://github.com/metio/aro/pull/7" false]
           (forge/ensure-pull-request! repository "krei/sync" "title" "body")))
    (is (= [["pr" "list" "--repo" "github.com/metio/aro" "--head" "krei/sync" "--state" "open"
             "--json" "url" "--jq" ".[0].url // empty"]]
           (calls)))))

(deftest a-missing-pull-request-is-opened
  (binding [forge/*program* (stub! (str "case \"$2\" in create) echo 'Creating pull request'; "
                                        "echo https://github.com/metio/aro/pull/8;; esac"))]
    (is (= ["https://github.com/metio/aro/pull/8" true]
           (forge/ensure-pull-request! repository "krei/sync" "title" "a body\nwith lines")))
    (is (= ["pr" "create" "--repo" "github.com/metio/aro" "--head" "krei/sync" "--base" "main"
            "--title" "title" "--body" "a body" "with lines"]
           (second (calls))))))

(deftest a-failing-gh-is-a-forge-problem-with-the-first-line-of-its-error
  (binding [forge/*program* (stub! "echo 'gh: not logged in' >&2; echo 'run gh auth login' >&2; exit 4")]
    (let [e (try (forge/open-pull-request repository "krei/sync") nil
                 (catch clojure.lang.ExceptionInfo e e))]
      (is (= :forge (:krei/problem (ex-data e))))
      (is (= "gh: not logged in" (:reason (ex-data e))))
      (is (str/includes? (ex-message e) "run gh auth login")))))

(deftest a-gh-that-overruns-its-deadline-is-a-forge-problem
  (with-redefs [forge/timeout-ms 300]
    (binding [forge/*program* (stub! "sleep 30")]
      (let [started (System/nanoTime)
            e (try (forge/open-pull-request repository "krei/sync") nil
                   (catch clojure.lang.ExceptionInfo e e))]
        (is (= :forge (:krei/problem (ex-data e))))
        (is (= "gh did not finish within 300 ms" (:reason (ex-data e))))
        (is (< (- (System/nanoTime) started) 10000000000) "the stand-in was killed, not waited for")))))

(deftest a-missing-gh-is-a-forge-problem
  (binding [forge/*program* (support/path "no-such-gh")]
    (let [e (try (forge/open-pull-request repository "krei/sync") nil
                 (catch clojure.lang.ExceptionInfo e e))]
      (is (= :forge (:krei/problem (ex-data e))))
      (is (str/starts-with? (:reason (ex-data e)) "could not run the program")))))
