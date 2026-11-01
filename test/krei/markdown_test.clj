;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.markdown-test
  (:require [clojure.test :refer [deftest is]]
            [krei.markdown :as markdown]))

(def ^:private readme
  (str "<!--\nSPDX-FileCopyrightText: Old\n-->\n\n# Old\n\nIntro.\n\n"
       "## Usage\n\n```sh\n## not a heading\n```\n\n"
       "## License\n\nOld.\n\n"
       "## Notes\n\nKept  \n"))

(deftest parts-rebuild-the-file-byte-for-byte
  (is (= ["Usage" "License" "Notes"] (keep :heading (markdown/parse readme))))
  (is (= readme (apply str (map :text (markdown/parse readme)))))
  (is (= readme (markdown/edit readme {}))))

(deftest a-header-replaces-the-comment-title-and-logo
  (is (= "# New\n\nIntro.\n" (markdown/edit "<!-- x -->\n# Old\n<img src=\"a\">\n\n\nIntro.\n" {:header "# New\n\n"}))))

(deftest a-section-is-set-where-it-stands
  (is (= (str "<!--\nSPDX-FileCopyrightText: Old\n-->\n\n# Old\n\nIntro.\n\n"
              "## Usage\n\n```sh\n## not a heading\n```\n\n"
              "## License\n\nNew.\n\n"
              "## Notes\n\nKept  \n")
         (markdown/edit readme {:sections [{:heading "License" :body "New."}]}))))

(deftest a-section-moves-to-its-place
  (is (= "Intro.\n\n## Notes\n\nKept\n\n## License\n\nNew.\n"
         (markdown/edit "Intro.\n\n## License\n\nOld.\n\n## Notes\n\nKept\n" {:sections [{:heading "License" :body "New.\n" :place :end}]})))
  (is (= "Intro.\n\n## Mirrors\n\nM.\n\n## License\n\nL.\n"
         (markdown/edit "Intro.\n\n## License\n\nL.\n" {:sections [{:heading "Mirrors" :body "M." :place {:before "License"}}]})))
  (is (= "Intro.\n\n## A\n\nA.\n\n## B\n\nB.\n"
         (markdown/edit "Intro.\n\n## B\n\nB.\n" {:sections [{:heading "A" :body "A." :place :start}]})))
  (is (= "Intro.\n\n## B\n\nB.\n\n## A\n\nA.\n"
         (markdown/edit "Intro.\n\n## B\n\nB.\n" {:sections [{:heading "A" :body "A." :place {:after "B"}}]})))
  (is (= "Intro.\n\n## A\n\nA.\n"
         (markdown/edit "Intro.\n" {:sections [{:heading "A" :body "A." :place {:before "Missing"}}]}))))

(deftest an-absent-section-is-removed
  (is (= "Intro.\n\n## Notes\n\nKept\n"
         (markdown/edit "Intro.\n\n## License\n\nOld.\n\n## Notes\n\nKept\n" {:sections [{:heading "License" :body :absent}]}))))

(deftest a-set-section-is-stable
  (let [once (markdown/edit readme {:sections [{:heading "License" :body "New." :place :end}]})]
    (is (= once (markdown/edit once {:sections [{:heading "License" :body "New." :place :end}]})))))
