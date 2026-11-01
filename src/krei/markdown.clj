;; SPDX-FileCopyrightText: The krei Authors
;; SPDX-License-Identifier: CC0-1.0

(ns krei.markdown
  "A Markdown file as its parts: the preamble before the first second-level
  heading, then one section per `## ` heading line, up to the next one. A
  line inside a fenced code block never starts a section. The parts hold
  their text as the file has it, so a file is rebuilt byte for byte from
  the parts an edit leaves alone."
  (:require [clojure.string :as str]))

(defn- fence-after
  "The marker of the fenced code block open after `line`, given `open`, the
  one open before it, or nil."
  [open line]
  (let [[_ indent marker] (re-find #"^( {0,3})(`{3,}|~{3,})" line)]
    (cond
      (nil? marker) open
      (nil? open) marker
      (and (= (first marker) (first open))
           (<= (count open) (count marker))
           (str/blank? (subs line (+ (count indent) (count marker))))) nil
      :else open)))

(defn- heading [line]
  (second (re-matches #"## +(.*?)(?: +#+)? *\n?" line)))

(defn parse
  "The parts of `text`: [{:heading nil :text preamble} {:heading h :text …} …]."
  [text]
  (loop [[line & more :as lines] (re-seq #"[^\n]*\n|[^\n]+" text)
         open nil
         parts [{:heading nil :text ""}]]
    (if-not (seq lines)
      parts
      (let [h (when-not open (heading line))]
        (recur more
               (fence-after open line)
               (if h
                 (conj parts {:heading (str/trim h) :text line})
                 (update-in parts [(dec (count parts)) :text] str line)))))))

(defn- joined
  "The text of `parts`. A part krei writes is set off from its neighbours
  by exactly one blank line, and the file ends with one newline after it."
  [parts]
  (reduce (fn [acc [before part]]
            (if (and (seq acc) (or (:managed part) (:managed before)))
              (str (str/trimr acc) "\n\n" (:text part))
              (str acc (:text part))))
          ""
          (map vector (cons nil parts) parts)))

(def ^:private header-pattern
  "The header of a preamble: an HTML comment, a first-level heading line and
  an <img> line, each optional, in that order, with the blank lines around them."
  #"(?s)\A(?:\s*<!--.*?-->)?(?:\s*# [^\n]*\n)?(?:\s*<img [^\n]*>\n)?\s*")

(defn- with-header [[preamble & sections] header]
  (cons (update preamble :text #(str header (subs % (count (re-find header-pattern %))))) sections))

(defn- section-text [heading body]
  (str "## " heading "\n\n" (str/replace body #"\A\n+|\s+\z" "") "\n"))

(defn- index-of [parts heading]
  (first (keep-indexed #(when (= heading (:heading %2)) %1) parts)))

(defn- with-section
  "`parts` with the section `heading` holding `body`, or without it when
  `body` is :absent. :place puts it at :start, right after the preamble, at
  :end, or {:before h} or {:after h} another section, falling back to the
  end when that section is missing; without :place the section stays where
  it is."
  [parts {:keys [heading body place]}]
  (let [here (index-of parts heading)
        others (vec (remove #(= heading (:heading %)) parts))]
    (if (= :absent body)
      others
      (let [part {:heading heading :text (section-text heading body) :managed true}
            at (cond
                 (= :start place) 1
                 (= :end place) (count others)
                 (:before place) (or (index-of others (:before place)) (count others))
                 (:after place) (if-let [i (index-of others (:after place))] (inc i) (count others))
                 here here
                 :else (count others))]
        (vec (concat (subvec others 0 at) [part] (subvec others at)))))))

(defn edit
  "`text` with the :header that `spec` names in place of the header of its
  preamble and each of its :sections set, one after the other. :header and
  each section's :body are already rendered."
  [text {:keys [header sections]}]
  (let [parts (cond-> (parse text) header (with-header header))]
    (joined (reduce with-section (vec parts) sections))))
