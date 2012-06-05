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


(defmulti check-ast (fn [_ expected] (class expected)))

(defmethod check-ast java.util.Map [actual expected]
  (def actual-tag (:tag actual))
  (is (contains? expected actual-tag)) ;; check parent
  (check-ast (:content actual) (actual-tag expected))) ;; recurse sub tree

(defmethod check-ast java.util.List [actual expected]
  (doseq [[actual-node expected-tag] (map vector actual expected)]
    (check-ast actual-node expected-tag)))

(defmethod check-ast clojure.lang.Keyword [actual expected]
  (is (= (:tag actual) expected)))



;; assignment tests

(deftest assignment-number
  (check-ast (isla-parser "age is 1")
             {:root
              [{:assignment
                [:atom :ws :is :ws {:value
                                    [:number]}]}]})
  )

(deftest assignment-variable
  (check-ast (isla-parser "age is x")
             {:root
              [{:assignment
                [:atom :ws :is :ws {:value
                                    [:atom]}]}]})
  )