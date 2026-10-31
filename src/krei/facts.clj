;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts
  "What krei knows about one repository. Later sources override earlier
  ones: the inventory record, facts read from the checkout, then the config
  under \"*\", under glob patterns from the least to the most specific,
  under the exact repository id, and the repository's own .krei.edn. Maps
  merge key by key. Facts derived from others fill in last, for whatever
  none of the sources set."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [cheshire.core :as json]
            [krei.inventory :as inventory]
            [selmer.filters :as filters]
            [selmer.parser :as selmer]
            [selmer.util :as selmer-util]))

(def markers
  "Facts that are true when the checkout holds a file matching one of these
  globs."
  {:go ["go.mod"]
   :maven ["pom.xml"]
   :cargo ["Cargo.toml"]
   :npm ["package.json"]
   :babashka ["bb.edn"]
   :clojure ["deps.edn"]
   :nix ["flake.nix" "shell.nix"]
   :breto [".breto.edn"]
   :container ["Dockerfile" "Containerfile"]
   :helm ["Chart.yaml" "charts/*/Chart.yaml"]
   :hugo ["docs/hugo.toml" "docs/hugo.yaml" "docs/config.toml" "docs/config.yaml"]
   :terraform-provider ["terraform-registry-manifest.json"]
   :reuse-toml ["REUSE.toml"]
   :vale [".vale.ini"]})

(defn- holds? [dir pattern]
  (boolean (seq (fs/glob dir pattern {:hidden true}))))

(defn detect
  "Facts read from the checkout at `dir`: each of `markers` is true when one
  of its globs matches a file. :licenses lists the identifiers of the
  licenses in LICENSES/, and :license is the only one, absent when there are
  several or none."
  [dir]
  (let [licenses (when (fs/directory? (fs/path dir "LICENSES"))
                   (sort (map #(str (fs/strip-ext (fs/file-name %))) (fs/glob (fs/path dir "LICENSES") "*.txt"))))]
    (cond-> (into {} (for [[fact patterns] markers :when (some #(holds? dir %) patterns)] [fact true]))
      (seq licenses) (assoc :licenses (vec licenses))
      (= 1 (count licenses)) (assoc :license (first licenses)))))

(defn- specificity [pattern]
  (count (str/replace pattern #"[*?\[\]{}]" "")))

(defn deep-merge
  "Merges maps into each other key by key; any other value replaces the one
  before it."
  [& ms]
  (apply merge-with (fn [a b] (if (and (map? a) (map? b)) (deep-merge a b) b)) ms))

(defn configured
  "The facts the config `facts` holds for repository `id`, merged in order
  of specificity."
  [facts id]
  (let [patterns (->> (keys facts)
                      (remove #{"*" id})
                      (filter #(inventory/glob-matches? % id))
                      (sort-by (juxt specificity identity)))]
    (apply deep-merge (get facts "*") (concat (map #(get facts %) patterns) [(get facts id)]))))

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

(defn own
  "The facts the repository checked out at `dir` states about itself in
  .krei.edn, so its exceptions live next to the files they shape."
  [dir]
  (let [p (fs/path dir ".krei.edn")]
    (when (fs/regular-file? p)
      (let [x (edn/read-string (slurp (str p)))]
        (when-not (or (nil? x) (map? x))
          (throw (ex-info ".krei.edn holds no map" {:krei/problem :fact})))
        x))))

(defn facts
  "Every fact about `repository`, checked out at `dir`, under `config`."
  [config repository dir]
  (derive-facts (deep-merge (dissoc repository :url) (detect dir) (configured (:facts config) (:id repository)) (own dir))))

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

(defn- sorted [x]
  (cond (map? x) (into (sorted-map-by #(compare (name %1) (name %2))) (map (fn [[k v]] [k (sorted v)])) x)
        (sequential? x) (mapv sorted x)
        :else x))

(filters/add-filter! :json (fn [x] (json/generate-string (sorted x) {:pretty {:indentation 2 :object-field-value-separator ": " :indent-arrays? true}})))

(defn render
  "`template`, a Selmer template, filled in with `facts`. A {{value}} naming
  a fact that is not set is an error, so a rule never writes a half-filled
  file; {% if %} and {% for %} treat it as empty. Nothing is escaped."
  [template facts]
  (selmer-util/without-escaping (selmer/render template (plain facts))))
