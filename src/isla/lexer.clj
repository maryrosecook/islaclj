(ns isla.lexer
  (:use [clojure.pprint])
  (:require [clojure.string :as str]))

(defn split-a-line [line]
  (vec (re-seq #"[^\s\"']+|\"[^\"]*\"|'[^']*'"
                       (str/trim line))))

(defn chuck-empties [lines]
  (reduce (fn [acc line]
            (if (= :nl line (peek acc))
              acc
              (conj acc line)))
          [] lines))

(defn lex [string]
  (let [tokens (chuck-empties (mapcat (fn [x]
                                        (-> (split-a-line x)
                                            (conj :nl)))
                                      (str/split-lines string)))]
    ;; add final newline if required
    (if (not= :nl (last tokens))
      (conj tokens :nl)
      tokens)))
