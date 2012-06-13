(ns isla.lexer
  (:use [clojure.pprint])
  (:require [clojure.string :as str]))

(defn split-a-line [line]
  (vec (re-seq #"[^\s\"']+|\"[^\"]*\"|'[^']*'"
                       (str/trim line))))

(defn lex [string]
  (let [tokens (flatten (map
                         (fn [x] (conj x :nl))
                         (map
                          (fn [x] (split-a-line x))
                          (str/split-lines string))))]

    ;; add final newline if required
    (if (not= :nl (last tokens))
      (conj tokens :nl)
      tokens)))

;; get rid of multiple newlines