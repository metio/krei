;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.forge
  "Pull requests for the branch krei pushes, opened with the gh command
  line on GitHub hosts."
  (:require [briko.process :as process]
            [clojure.string :as str]))

(defn supported? [{:keys [host]}]
  (= "github.com" host))

(def ^:dynamic *program*
  "The gh command line krei runs."
  "gh")

(def timeout-ms
  "How long one gh call may take: two minutes. Each call is one or two
  requests to the GitHub API; a stalled one becomes a forge problem for that
  repository, which leaves its branch pushed, instead of holding the whole
  run."
  120000)

(defn- gh! [& args]
  (let [{:keys [exit out err timed-out?]}
        (process/run (into [*program*] args) {:timeout-ms timeout-ms})
        err (if timed-out? (str "gh did not finish within " timeout-ms " ms\n" err) err)]
    (when-not (zero? exit)
      (throw (ex-info (str "gh " (str/join " " args) ": " (str/trim err))
                      {:krei/problem :forge :reason (first (str/split-lines (str/trim err)))})))
    (str/trim out)))

(defn- slug [{:keys [host path]}]
  (str host "/" path))

(defn open-pull-request
  "The URL of the open pull request from `branch`, or nil."
  [repository branch]
  (let [url (gh! "pr" "list" "--repo" (slug repository) "--head" branch "--state" "open"
                 "--json" "url" "--jq" ".[0].url // empty")]
    (when-not (str/blank? url) url)))

(defn ensure-pull-request!
  "The URL of the pull request from `branch` into the default branch,
  opened when there is none. Returns [url opened?]."
  [{:keys [default-branch] :as repository} branch title body]
  (if-let [url (open-pull-request repository branch)]
    [url false]
    [(last (str/split-lines (gh! "pr" "create" "--repo" (slug repository) "--head" branch
                                 "--base" default-branch "--title" title "--body" body)))
     true]))
