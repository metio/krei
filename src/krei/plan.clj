;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.plan
  "Comparing one repository with its rules, and proposing the difference
  as a pull request."
  (:require [clojure.string :as str]
            [krei.facts :as facts]
            [krei.forge :as forge]
            [krei.git :as git]
            [krei.rules :as rules]))

(defn inspect
  "The changes `repository` needs for `rules` to hold on its default branch:
  {:repository :dir :changes}."
  [config rules repository]
  (let [dir (git/cache-dir (:cache config) repository)]
    (git/checkout! dir repository)
    {:repository repository
     :dir dir
     :changes (rules/changes rules dir (facts/facts config repository dir))}))

(defn describe [{:keys [action path rule]}]
  (str (name action) " " path " (" (name rule) ")"))

(defn- body [changes]
  (str/join "\n" (map #(str "- " (describe %)) changes)))

(def ^:dynamic *forge*
  "How pull requests are opened: {:supported? :ensure!}."
  {:supported? forge/supported?
   :ensure! forge/ensure-pull-request!})

(defn sync!
  "Commits the changes of `inspection` onto the krei branch, pushes it, and
  opens a pull request for it where the host supports one. Returns
  {:status :pushed?, :pull-request, :opened?}; :status is :in-sync when there
  is nothing to change, and :pushed without a pull request, with the forge's
  :reason when opening one failed."
  [{:keys [branch message signoff]} {:keys [repository dir changes]}]
  (if (empty? changes)
    {:status :in-sync}
    (do (rules/apply! dir changes)
        (git/commit! dir branch (str message "\n\n" (body changes)) signoff)
        (let [pushed? (git/publish! dir branch)
              {:keys [supported? ensure!]} *forge*]
          (if (supported? repository)
            (try
              (let [[url opened?] (ensure! repository branch message (body changes))]
                {:status :proposed :pushed? pushed? :pull-request url :opened? opened?})
              (catch clojure.lang.ExceptionInfo e
                (if (= :forge (:krei/problem (ex-data e)))
                  {:status :pushed :pushed? pushed? :reason (:reason (ex-data e))}
                  (throw e))))
            {:status :pushed :pushed? pushed?})))))
