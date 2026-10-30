;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts
  "What krei knows about one repository. Later sources override earlier
  ones: the inventory record, facts read from the checkout, then the config
  under \"*\", under glob patterns from the least to the most specific, and
  under the exact repository id. Facts derived from others fill in last,
  for whatever none of the sources set."
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [krei.inventory :as inventory]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]))

(def ^:private markers
  "Facts that are true when the checkout holds a file of this name."
  {:go "go.mod" :maven "pom.xml" :cargo "Cargo.toml" :npm "package.json" :babashka "bb.edn" :nix "flake.nix"})

(defn detect
  "Facts read from the checkout at `dir`. :license is the identifier of the
  only license in LICENSES/, and absent when there are several or none.
  :go, :maven, :cargo, :npm, :babashka and :nix are true when the build
  file of that tool is at the top of the checkout."
  [dir]
  (let [licenses (when (fs/directory? (fs/path dir "LICENSES"))
                   (fs/glob (fs/path dir "LICENSES") "*.txt"))]
    (cond-> (into {} (for [[fact file] markers :when (fs/regular-file? (fs/path dir file))] [fact true]))
      (= 1 (count licenses)) (assoc :license (str (fs/strip-ext (fs/file-name (first licenses))))))))

(defn- specificity [pattern]
  (count (str/replace pattern #"[*?\[\]{}]" "")))

(defn configured
  "The facts the config `facts` holds for repository `id`, merged in order
  of specificity."
  [facts id]
  (let [patterns (->> (keys facts)
                      (remove #{"*" id})
                      (filter #(inventory/glob-matches? % id))
                      (sort-by (juxt specificity identity)))]
    (apply merge (get facts "*") (concat (map #(get facts %) patterns) [(get facts id)]))))

(def ^:private roles
  {:owner :owners :maintainer :maintainers :contributor :contributors :translator :translators})

(defn derive-facts
  "`facts` with the derived ones it does not set itself: :title is the
  name, :copyright names the title's authors, :homepage is the web page of
  the repository, :keywords are its topics, and :owners, :maintainers,
  :contributors and :translators split :authors by their :role, which is
  :owner unless set."
  [{:keys [name host path topics authors] :as facts}]
  (let [title (or (:title facts) name)
        by-role (group-by #(keyword (or (:role %) :owner)) authors)]
    (merge (cond-> {:keywords (vec topics)}
             title (assoc :title title :copyright (str "The " title " Authors"))
             (and host path) (assoc :homepage (str "https://" host "/" path "/")))
           (into {} (for [[role fact] roles] [fact (vec (get by-role role))]))
           facts)))

(defn facts
  "Every fact about `repository`, checked out at `dir`, under `config`."
  [config repository dir]
  (derive-facts (merge (dissoc repository :url) (detect dir) (configured (:facts config) (:id repository)))))

(defn- plain
  "`x` with every keyword value turned into its name, so templates print
  :tool as tool. Map keys stay keywords, since templates look facts up by
  them."
  [x]
  (cond (map? x) (into {} (map (fn [[k v]] [k (plain v)]) x))
        (set? x) (into #{} (map plain) x)
        (sequential? x) (mapv plain x)
        (keyword? x) (name x)
        :else x))

(selmer-util/set-missing-value-formatter!
 (fn [tag _]
   (if (= :filter (:tag-type tag))
     (throw (ex-info (str "no fact " (:tag-value tag) " for {{" (:tag-value tag) "}}") {:krei/problem :fact}))
     "")))

(defn render
  "`template`, a Selmer template, filled in with `facts`. A {{value}} naming
  a fact that is not set is an error, so a rule never writes a half-filled
  file; {% if %} and {% for %} treat it as empty. Nothing is escaped."
  [template facts]
  (selmer-util/without-escaping (selmer/render template (plain facts))))
