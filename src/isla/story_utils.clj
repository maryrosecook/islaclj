(ns isla.story-utils)

(defn take-input []
  (print "$ ")
  (flush)
  (read-line))

(defn output [given-output]
  (println " " given-output))

(defn instantiate-type [type-hash]
  (clojure.lang.Reflector/invokeConstructor
   (:type type-hash)
   (to-array (:defaults type-hash))))
