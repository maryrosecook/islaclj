(ns isla.story-utils
  (:require [clojure.string :as str]))

(defn take-input []
  (print "$ ")
  (flush)
  (read-line))

(defn output [& outs]
  (let [output (reduce (fn [acc x]
                         (str acc "  " x "\n"))
                       "" (str/split-lines (apply str outs)))]
    (println output)
    output))