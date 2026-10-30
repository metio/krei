;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.support
  (:require [krei.git :as git]
            [babashka.fs :as fs]
            [babashka.process :as process]
            [clojure.string :as str]))

(def ^:dynamic *root* nil)

(defn path [& parts]
  (str (apply fs/path *root* parts)))

(defn with-root
  "Runs `f` in a fresh directory, with git reading only a config of this
  test's own, so commits need no signing key and identity of the machine."
  [f]
  (let [root (str (fs/canonicalize (fs/create-temp-dir {:prefix "krei-test"})))
        gitconfig (str (fs/path root "gitconfig"))]
    (spit gitconfig "[user]\n\tname = krei\n\temail = krei@example.org\n[init]\n\tdefaultBranch = main\n[commit]\n\tgpgsign = false\n[advice]\n\tdetachedHead = false\n")
    (try
      (binding [*root* root
                git/*environment* {"GIT_CONFIG_GLOBAL" gitconfig "GIT_CONFIG_NOSYSTEM" "1"}]
        (f))
      (finally (fs/delete-tree root)))))

(defn git
  "Runs git in `dir` and returns its trimmed output, throwing on failure."
  [dir & args]
  (let [{:keys [exit out err]} (apply process/shell {:out :string :err :string :continue true :dir dir
                                                     :extra-env git/*environment*}
                                      "git" args)]
    (when-not (zero? exit)
      (throw (ex-info (str "git " (str/join " " args) ": " err) {})))
    (str/trimr out)))

(defn commit!
  "Writes `files`, {relative-path content}, in `dir` and commits them."
  [dir message files]
  (doseq [[file content] files]
    (fs/create-dirs (fs/parent (fs/path dir file)))
    (spit (str (fs/path dir file)) content))
  (git dir "add" "--all")
  (git dir "commit" "--quiet" "--message" message))

(defn upstream!
  "A bare repository at `name`.git whose main holds `files`, and a work
  clone of it at `name`-work for pushing more commits."
  [name files]
  (let [bare (path (str name ".git"))
        work (path (str name "-work"))]
    (git *root* "init" "--quiet" "--bare" bare)
    (git *root* "clone" "--quiet" bare work)
    (commit! work "initial" files)
    (git work "push" "--quiet" "origin" "main")
    {:url bare :work work}))

(defn push! [{:keys [work]} message files]
  (commit! work message files)
  (git work "push" "--quiet" "origin" "main"))
