;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.plan-test
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.config :as config]
            [krei.plan :as plan]
            [krei.rules :as rules]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(defn- setup [files]
  (let [upstream (support/upstream! "aro" files)
        config (assoc (config/defaults) :cache (support/path "cache") :signoff true)
        repository {:id "example.org/metio/aro" :host "example.org" :path "metio/aro" :name "aro"
                    :default-branch "main" :url (:url upstream)}]
    {:upstream upstream :config config :repository repository
     :rules [(rules/load-rule [] :license) (rules/load-rule [] :no-golangci)]}))

(defn- remote-file [{:keys [url]} branch file]
  (support/git support/*root* "--git-dir" url "show" (str branch ":" file)))

(deftest inspect-reports-what-the-default-branch-needs
  (let [{:keys [config rules repository]} (setup {"LICENSES/0BSD.txt" "zero" ".golangci.yml" "x"})
        {:keys [changes]} (plan/inspect config rules repository)]
    (is (= ["create LICENSE (license)" "delete .golangci.yml (no-golangci)"]
           (map plan/describe changes)))))

(deftest apply-into-writes-the-changes-beside-local-edits
  (let [{:keys [config rules repository]} (setup {"README.md" "x"})
        dir (support/path "work")]
    (doseq [[file content] {"LICENSES/0BSD.txt" "zero" ".golangci.yml" "x" "pom.xml" "local"}]
      (fs/create-dirs (fs/parent (fs/path dir file)))
      (spit (str (fs/path dir file)) content))
    (is (= ["create LICENSE (license)" "delete .golangci.yml (no-golangci)"]
           (map plan/describe (plan/apply-into! config rules repository dir))))
    (is (= "zero" (slurp (str dir "/LICENSE"))))
    (is (not (fs/exists? (fs/path dir ".golangci.yml"))))
    (is (= "local" (slurp (str dir "/pom.xml"))))))

(deftest sync-pushes-the-branch-and-proposes-it-once
  (let [{:keys [upstream config rules repository]} (setup {"LICENSES/0BSD.txt" "zero"})
        proposals (atom [])]
    (binding [plan/*forge* {:supported? (constantly true)
                            :ensure! (fn [_ branch title body]
                                       (swap! proposals conj [branch title body])
                                       ["https://example.org/pr/1" (= 1 (count @proposals))])}]
      (is (= {:status :proposed :pushed? true :pull-request "https://example.org/pr/1" :opened? true}
             (plan/sync! config (plan/inspect config rules repository))))
      (is (= "zero" (remote-file upstream "krei/sync" "LICENSE")))
      (is (str/includes? (support/git support/*root* "--git-dir" (:url upstream) "log" "-1" "--format=%B" "krei/sync")
                         "Signed-off-by: krei <krei@example.org>"))
      (testing "a second run with nothing new pushes nothing"
        (is (= {:status :proposed :pushed? false :pull-request "https://example.org/pr/1" :opened? false}
               (plan/sync! config (plan/inspect config rules repository)))))
      (testing "the branch follows a new default branch"
        (support/push! upstream "more" {"README.md" "hello"})
        (is (true? (:pushed? (plan/sync! config (plan/inspect config rules repository)))))
        (is (= "hello" (remote-file upstream "krei/sync" "README.md"))))
      (testing "the branch moves onto a new default branch that leaves the same files"
        (support/push! upstream "same files" {"LICENSE" "other"})
        (is (true? (:pushed? (plan/sync! config (plan/inspect config rules repository)))))
        (is (= (support/git support/*root* "--git-dir" (:url upstream) "rev-parse" "main")
               (support/git support/*root* "--git-dir" (:url upstream) "rev-parse" "krei/sync^"))))
      (testing "once merged the repository follows its rules"
        (support/push! upstream "merge" {"LICENSE" "zero"})
        (is (= {:status :in-sync} (plan/sync! config (plan/inspect config rules repository))))))))

(deftest sync-without-forge-support-only-pushes
  (let [{:keys [upstream config rules repository]} (setup {"LICENSES/0BSD.txt" "zero"})]
    (binding [plan/*forge* {:supported? (constantly false) :ensure! (fn [& _] (throw (ex-info "unexpected" {})))}]
      (is (= {:status :pushed :pushed? true} (plan/sync! config (plan/inspect config rules repository))))
      (is (= "zero" (remote-file upstream "krei/sync" "LICENSE"))))))

(deftest sync-keeps-the-push-when-the-forge-fails
  (let [{:keys [upstream config rules repository]} (setup {"LICENSES/0BSD.txt" "zero"})
        unauthenticated (fn [& _] (throw (ex-info "gh pr list: not logged in"
                                                  {:krei/problem :forge :reason "not logged in"})))]
    (binding [plan/*forge* {:supported? (constantly true) :ensure! unauthenticated}]
      (is (= {:status :pushed :pushed? true :reason "not logged in"}
             (plan/sync! config (plan/inspect config rules repository))))
      (is (= "zero" (remote-file upstream "krei/sync" "LICENSE")))
      (testing "a second run still names the reason"
        (is (= {:status :pushed :pushed? false :reason "not logged in"}
               (plan/sync! config (plan/inspect config rules repository))))))))

(deftest inspect-starts-from-a-clean-checkout
  (let [{:keys [config rules repository]} (setup {"LICENSES/0BSD.txt" "zero"})
        {:keys [dir]} (plan/inspect config rules repository)]
    (spit (str dir "/LICENSE") "leftover")
    (spit (str dir "/stray.txt") "junk")
    (is (= ["create LICENSE (license)"] (map plan/describe (:changes (plan/inspect config rules repository)))))))
