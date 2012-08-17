(ns isla.story-utils)

(defn take-input []
  (print "$ ")
  (flush)
  (read-line))

(defn output [& outs]
  (apply println " " outs))
