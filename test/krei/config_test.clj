;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.config-test
  (:require [babashka.fs :as fs]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [krei.config :as config]
            [krei.support :as support]))

(use-fixtures :each support/with-root)

(defn- environment
  "Options that resolve paths against `variables` and a user.home of /passwd."
  [variables]
  {:getenv #(get variables %) :getprop #(when (= "user.home" %) "/passwd")})

(defn- refusal [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:type (ex-data e)))))

(deftest defaults-follow-the-xdg-variables
  (let [opts (environment {"HOME" "/home/ada" "XDG_CONFIG_HOME" "/xdg/config"
                           "XDG_STATE_HOME" "/xdg/state" "XDG_CACHE_HOME" "/xdg/cache"})]
    (is (= "/xdg/config/krei/config.edn" (config/default-path opts)))
    (is (= {:inventory "/xdg/state/aro/inventory.edn"
            :rule-dirs ["/xdg/config/krei/rules"]
            :cache "/xdg/cache/krei"}
           (select-keys (config/defaults opts) [:inventory :rule-dirs :cache])))))

(deftest defaults-fall-back-to-home-and-ignore-relative-variables
  (let [opts (environment {"HOME" "/home/ada" "XDG_CONFIG_HOME" "relative" "XDG_STATE_HOME" ""})]
    (is (= "/home/ada/.config/krei/config.edn" (config/default-path opts)))
    (is (= "/home/ada/.local/state/aro/inventory.edn" (:inventory (config/defaults opts))))
    (is (= "/home/ada/.cache/krei" (:cache (config/defaults opts)))))
  (testing "user.home stands in for a HOME that is unset or relative"
    (is (= "/passwd/.config/krei/config.edn" (config/default-path (environment {"HOME" "relative"}))))))

(deftest load-config-reads-over-the-defaults-and-expands-home
  (let [file (support/path "config.edn")
        opts (environment {"HOME" "/home/ada"})]
    (spit file (pr-str {:inventory "~/inventory.edn" :cache "~/cache" :rule-dirs ["~/rules" "/abs" "~other/x"]
                        :include ["github.com/metio/*"]}))
    (let [loaded (config/load-config file opts)]
      (is (= "/home/ada/inventory.edn" (:inventory loaded)))
      (is (= "/home/ada/cache" (:cache loaded)))
      (is (= ["/home/ada/rules" "/abs" "~other/x"] (:rule-dirs loaded)))
      (is (= ["github.com/metio/*"] (:include loaded)))
      (is (= [:license] (:rules loaded))))))

(deftest a-missing-config-means-all-defaults
  (let [opts (environment {"HOME" "/home/ada"})]
    (is (= (config/defaults opts) (config/load-config (support/path "missing.edn") opts)))))

(deftest a-config-without-a-form-means-all-defaults
  (let [file (support/path "config.edn")
        opts (environment {"HOME" "/home/ada"})]
    (doseq [content ["" "  \n\t\n" ";; nothing configured yet\n" "#_{:rules []}\n"]]
      (spit file content)
      (is (= (config/defaults opts) (config/load-config file opts)) (pr-str content)))))

(deftest a-config-link-to-nothing-means-all-defaults
  (let [file (support/path "config.edn")
        opts (environment {"HOME" "/home/ada"})]
    (fs/create-sym-link file (support/path "missing.edn"))
    (is (= (config/defaults opts) (config/load-config file opts)))
    (spit (support/path "missing.edn") "{:rules []}")
    (is (= [] (:rules (config/load-config file opts))) "a link to a config is read through")))

(deftest a-config-holds-exactly-one-map-within-the-bounds
  (let [file (support/path "config.edn")
        opts (environment {"HOME" "/home/ada"})]
    (spit file "[:license]")
    (is (= :briko.edn/unexpected (refusal #(config/load-config file opts))))
    (spit file "{:rules [:license]} {:rules []}")
    (is (= :briko.edn/trailing-form (refusal #(config/load-config file opts))))
    (spit file "{:facts {\"*\" {:since #inst \"2026-01-01\"}}}")
    (is (= :briko.edn/tag-refused (refusal #(config/load-config file opts))))
    (spit file (str "{:facts " (apply str (repeat 70 "{:a ")) (apply str (repeat 70 "}")) "}"))
    (is (= :briko.edn/too-deep (refusal #(config/load-config file opts))))
    (spit file (str "{:message \"" (apply str (repeat config/max-bytes "x")) "\"}"))
    (is (= :briko.edn/too-large (refusal #(config/load-config file opts))))))
