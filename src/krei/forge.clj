;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.forge
  "Pull requests for the branch krei pushes, opened with the gh command
  line on GitHub hosts."
  (:require [babashka.process :as process]
            [clojure.string :as str]))

(defn supported? [{:keys [host]}]
  (= "github.com" host))

(defn- gh! [& args]
  (let [{:keys [exit out err]} (apply process/shell {:out :string :err :string :continue true} "gh" args)]
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
