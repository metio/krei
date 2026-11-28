;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.facts
  "What krei knows about one repository. Later sources override earlier
  ones: the inventory record, facts read from the checkout, then the config
  under \"*\", under custom properties from those naming the fewest, under
  glob patterns from the least to the most specific, under the exact
  repository id, and the repository's own .krei.edn. Maps merge key by key.
  Facts derived from others fill in last, for whatever none of the sources
  set."
  (:require [babashka.fs :as fs]
            [briko.edn :as edn]
            [clojure.string :as str]
            [cheshire.core :as json]
            [clj-yaml.core :as yaml]
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
   :breto [".breto.edn"]
   :container ["Dockerfile" "Containerfile"]
   :helm ["Chart.yaml" "charts/*/Chart.yaml"]
   :hugo ["docs/hugo.toml" "docs/hugo.yaml" "docs/config.toml" "docs/config.yaml"]
   :nix ["flake.nix" "shell.nix"]
   :terraform ["**.tf"]
   :terraform-provider ["terraform-registry-manifest.json"]
   :reuse-toml ["REUSE.toml"]
   :reuse-workflow [".github/workflows/reuse.yml"]
   :vale [".vale.ini"]})

(defn- holds? [dir pattern]
  (boolean (seq (fs/glob dir pattern {:hidden true}))))

(defn detect
  "Facts read from the checkout at `dir`: each of `markers` is true when one
  of its globs matches a file, and :licenses lists the identifiers of the
  licenses in LICENSES/."
  [dir]
  (let [licenses (when (fs/directory? (fs/path dir "LICENSES"))
                   (sort (map #(str (fs/strip-ext (fs/file-name %))) (fs/glob (fs/path dir "LICENSES") "*.txt"))))]
    (cond-> (into {} (for [[fact patterns] markers :when (some #(holds? dir %) patterns)] [fact true]))
      (seq licenses) (assoc :licenses (vec licenses)))))

(defn- specificity [pattern]
  (count (str/replace pattern #"[*?\[\]{}]" "")))

(defn deep-merge
  "Merges maps into each other key by key; any other value replaces the one
  before it."
  [& ms]
  (apply merge-with (fn [a b] (if (and (map? a) (map? b)) (deep-merge a b) b)) ms))

(defn- properties-hold?
  "Whether `repository` has every custom property `wanted` names with its
  value, which for a multi-select property is one of the values it holds."
  [repository wanted]
  (every? (fn [[k v]]
            (let [actual (get-in repository [:properties (keyword (name k))])]
              (if (sequential? actual) (some #{v} actual) (= v actual))))
          wanted))

(defn configured
  "The facts the config `facts` holds for `repository`, merged in order of
  specificity. A key that is a map selects the repositories by their custom
  properties, a string by their id."
  [facts {:keys [id] :as repository}]
  (let [selectors (->> (keys facts)
                       (filter map?)
                       (filter #(properties-hold? repository %))
                       (sort-by (juxt count pr-str)))
        patterns (->> (keys facts)
                      (filter string?)
                      (remove #{"*" id})
                      (filter #(inventory/glob-matches? % id))
                      (sort-by (juxt specificity identity)))]
    (apply deep-merge (get facts "*") (concat (map #(get facts %) (concat selectors patterns)) [(get facts id)]))))

(def ^:private roles
  {:owner :owners :maintainer :maintainers :contributor :contributors :translator :translators})

(defn derive-facts
  "`facts` with the derived ones it does not set itself: :title is the
  name, :copyright names the authors of the name, :homepage is the page of a
  Terraform provider in the Terraform Registry and the web page of any other
  repository, :keywords are its topics, and :owners, :maintainers,
  :contributors and :translators split :authors by their :role, which is
  :owner unless set."
  [{:keys [name namespace host path topics authors terraform-provider] :as facts}]
  (let [title (or (:title facts) name)
        by-role (group-by #(keyword (or (:role %) :owner)) authors)
        provider (when (and terraform-provider namespace name) (second (re-matches #"terraform-provider-(.+)" name)))]
    (merge (cond-> {:keywords (vec topics)}
             title (assoc :title title)
             name (assoc :copyright (str "The " name " Authors"))
             (and host path) (assoc :homepage (str "https://" host "/" path "/"))
             provider (assoc :homepage (str "https://registry.terraform.io/providers/" namespace "/" provider "/")))
           (into {} (for [[role fact] roles] [fact (vec (get by-role role))]))
           facts)))

(def max-own-bytes
  "The largest .krei.edn krei reads, in UTF-8 bytes: 64 KiB. Its facts are a
  repository's exceptions; a contributor's commit cannot make krei read more."
  65536)

(defn own
  "The facts the repository checked out at `dir` states about itself in
  .krei.edn, so its exceptions live next to the files they shape. A file
  holding no form, only comments, states nothing."
  [dir]
  (let [p (fs/path dir ".krei.edn")]
    (when (fs/regular-file? p)
      (let [forms (try (edn/read-file (str p) {:max-bytes max-own-bytes :one-form? false})
                       (catch clojure.lang.ExceptionInfo e
                         (throw (ex-info (str ".krei.edn is no valid EDN: " (ex-message e)) {:krei/problem :fact} e))))
            x (case (count forms)
                0 nil
                1 (first forms)
                (throw (ex-info ".krei.edn holds more than one form" {:krei/problem :fact})))]
        (when-not (or (nil? x) (map? x))
          (throw (ex-info ".krei.edn holds no map" {:krei/problem :fact})))
        x))))

(defn- scheduled
  "`facts` with the :cron of its :release from `calendar`, which krei plans
  from the config alone, so a :release set anywhere else is an error."
  [facts calendar]
  (if-let [{:keys [cadence]} (:release facts)]
    (let [slot (get calendar (:id facts))]
      (when-not (= cadence (:cadence slot))
        (throw (ex-info (str "the :release of " (:id facts) " belongs in the config, where krei plans the release calendar")
                        {:krei/problem :fact})))
      (update facts :release merge (select-keys slot [:cron])))
    facts))

(defn facts
  "Every fact about `repository`, checked out at `dir`, under `config`, whose
  :calendar holds the release slots."
  [config repository dir]
  ;; :license is the one license the checkout, the config and .krei.edn
  ;; agree on after merging, so a configured :licenses outranks a stale file
  ;; in LICENSES/; the inventory's guess stands only when they name none
  (let [stated (deep-merge (detect dir) (configured (:facts config) repository) (own dir))
        licenses (:licenses stated)
        license (or (:license stated) (when (= 1 (count licenses)) (first licenses)))]
    (-> (deep-merge (dissoc repository :url) stated)
        (cond-> license (assoc :license license))
        (scheduled (:calendar config))
        derive-facts)))

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
(defn yaml-scalar
  "`x` as a YAML scalar to write after a key on the same line: plain where
  YAML reads it back as the same string, quoted where it would not, such as
  a title holding \": \" or a name like yes; a value with a line break
  becomes a double-quoted string, which stays on one line."
  [x]
  (let [s (str x)
        dumped (str/trimr (yaml/generate-string s))]
    (if (str/includes? dumped "\n") (json/generate-string s) dumped)))

(filters/add-filter! :yaml yaml-scalar)
(filters/add-filter! :words (fn [x] (vec (remove str/blank? (str/split (str x) #"\s+")))))
(filters/add-filter! :url (fn [x] (str "https://" (str/replace-first (str x) ":" "/"))))

(defn render
  "`template`, a Selmer template, filled in with `facts`. A {{value}} naming
  a fact that is not set is an error, so a rule never writes a half-filled
  file; {% if %} and {% for %} treat it as empty. Nothing is escaped."
  [template facts]
  (selmer-util/without-escaping (selmer/render template (plain facts))))
