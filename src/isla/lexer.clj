(ns isla.lexer
  (:use [clojure.pprint])
  (:require [clojure.string :as str]))

(defn lex [string]
  (def tokens (flatten (map
                        (fn [x] (conj x :nl))
                        (map
                         (fn [x] (str/split (str/trim x) #" +"))
                         (str/split-lines string)))))

  ;; add final newline if required
  (if (not= :nl (last tokens))
    (conj tokens :nl)
    tokens))

;; get rid of multiple newlines