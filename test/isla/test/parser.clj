(ns isla.test.parser
  (:use [isla.parser])
  (:use [clojure.test])
  (:use [clojure.pprint]))

(defmulti check-ast (fn [_ expected] (class expected)))

(defmethod check-ast java.util.Map [actual expected]
  (def actual-tag (:tag actual))
  (is (contains? expected actual-tag)) ;; check parent
  (check-ast (:content actual) (actual-tag expected))) ;; recurse sub tree

(defmethod check-ast java.util.List [actual expected]
  (is (= (count actual) (count expected))) ;; if not same len, got a problem
  (doseq [[actual-node expected-tag] (map vector actual expected)]
    (check-ast actual-node expected-tag)))

(defmethod check-ast :default [actual expected] ;; keyword, string, int
  (is (= actual expected)))

;; nnode

(deftest nnode-create
  (is (= (nnode :integer [1]) {:tag :integer :content [1]})))

;; assignment

(deftest assignment-number
  (check-ast (parse "mary is 1")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["mary"]}
                                  {:is [:is]}
                                  {:value [{:integer [1]}]}]}]}]}]}))

(deftest assignment-identifier
  (check-ast (parse "isla is age")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["isla"]}
                                  {:is [:is]}
                                  {:value [{:identifier ["age"]}]}]}]}]}]}))

(deftest assignment-string
  (check-ast (parse "isla is 'cool'")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["isla"]}
                                  {:is [:is]}
                                  {:value [{:string ["cool"]}]}]}]}]}]}))


;; blocks

(deftest two-expression-block
  (check-ast (parse "isla is 1\nmary is 2")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["isla"]}
                                  {:is [:is]}
                                  {:value [{:integer [1]}]}]}]}
                              {:expression
                               [{:assignment
                                 [{:assignee ["mary"]}
                                  {:is [:is]}
                                  {:value [{:integer [2]}]}]}]}]}]}))
