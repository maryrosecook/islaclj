(ns isla.test.core
  (:use [isla.peg])
  (:use [clojure.test])
  (:use [net.cgrand.parsley])
  (:use [clojure.pprint]))

(def isla-parser (parser {
                :main :assignment
                :space :ws?
                :root-tag :root
                }
               :assignment [:atom :is :value]
               :is "is"
               :atom #"[A-Za-z]+"
               :ws #"[ \t\r\n]+"
               :value #{:number :atom} ;; :string
               :number #"[0-9]+"
               ;; :comment
               ))

(defn get-content [ast]
  (get ast :content))

(defn get-first-statement [ast]
  (get-content (first (get-content ast))))

(defn check-tag-seq [ast tags]
  (doseq [[ast-node expected-tag] (map vector ast tags)]
    (is (= (get ast-node :tag) expected-tag))))

;; assignment tests

(deftest assignment
  (check-tag-seq (get-first-statement (isla-parser "atom is 1"))
                 [:atom :ws :is :ws :value]))