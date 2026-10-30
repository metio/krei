;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.git
  "Cache clones of managed repositories, and the branch krei proposes its
  changes on. Everything goes through the git command line."
  (:require [babashka.fs :as fs]
            [babashka.process :as process]
            [clojure.string :as str]))

(def ^:dynamic *environment*
  "Variables added to the environment of every git call."
  {})

(defn- environment []
  (merge (cond-> {"GIT_TERMINAL_PROMPT" "0"}
           (str/blank? (System/getenv "GIT_SSH_COMMAND")) (assoc "GIT_SSH_COMMAND" "ssh -o BatchMode=yes"))
         *environment*))

(defn git
  "Runs git in `dir` with `args` and returns {:exit :out :err}."
  [dir & args]
  (apply process/shell {:dir (str dir) :out :string :err :string :continue true :extra-env (environment)}
         "git" (map str args)))

(defn git!
  "Runs git in `dir` with `args` and returns its trimmed output, throwing on
  failure."
  [dir & args]
  (let [{:keys [exit out err]} (apply git dir args)]
    (when-not (zero? exit)
      (throw (ex-info (str "git " (str/join " " args) ": " (str/trim (str err " " out)))
                      {:krei/problem :git})))
    (str/trim out)))

(defn clone-url [{:keys [url host path]}]
  (or url (str host ":" path)))

(defn cache-dir [cache {:keys [host path]}]
  (str (fs/path cache host path)))

(defn checkout!
  "A clean checkout of the default branch of `repository` at `dir`, cloned
  on first use and fetched afterwards. Returns the commit checked out."
  [dir {:keys [default-branch] :as repository}]
  (if (fs/directory? (fs/path dir ".git"))
    (git! dir "fetch" "--quiet" "--prune" "origin")
    (do (fs/create-dirs (fs/parent dir))
        (git! (fs/parent dir) "clone" "--quiet" "--no-checkout" (clone-url repository) dir)))
  (let [start (str "origin/" default-branch)]
    (git! dir "checkout" "--quiet" "--force" "--detach" start)
    (git! dir "clean" "--quiet" "--force" "-d" "-x")
    (git! dir "rev-parse" "HEAD")))

(defn- remote-tree [dir branch]
  (let [{:keys [exit out]} (git dir "rev-parse" "--verify" "--quiet" (str "refs/remotes/origin/" branch "^{tree}"))]
    (when (zero? exit) (str/trim out))))

(defn commit!
  "Commits every change in the checkout at `dir` onto `branch`, started from
  the commit checked out."
  [dir branch message signoff]
  (git! dir "checkout" "--quiet" "-B" branch)
  (git! dir "add" "--all")
  (apply git! dir "commit" "--quiet" "--message" message (when signoff ["--signoff"])))

(defn publish!
  "Pushes `branch` to origin, replacing what is there. Returns false when
  origin already holds the same files there, so nothing was pushed."
  [dir branch]
  (if (= (remote-tree dir branch) (git! dir "rev-parse" "HEAD^{tree}"))
    false
    (do (git! dir "push" "--quiet" "--force" "origin" (str "HEAD:refs/heads/" branch))
        true)))
