(ns isla.test.parser
  (:use [isla.parser])
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:require [mrc.utils :as utils]))

(defmulti check-ast (fn [_ expected] (class expected)))

(defmethod check-ast java.util.Map [actual expected]
  (def actual-tag (:tag actual))
  (is (contains? expected actual-tag)) ;; check parent
  (check-ast (:c actual) (actual-tag expected))) ;; recurse sub tree

(defmethod check-ast java.util.List [actual expected]
  (is (= (count actual) (count expected))) ;; if not same len, got a problem
  (doseq [[actual-node expected-tag] (map vector actual expected)]
    (check-ast actual-node expected-tag)))

(defmethod check-ast :default [actual expected] ;; keyword, string, int
  (is (= actual expected)))

;; nnode

(deftest nnode-create
  (is (= (nnode :integer [1]) {:tag :integer :c [1]})))

;; slot assignment

(deftest test-slot-assignment
  (check-ast (parse "isla age is 1")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:object
                                               [{:identifier ["isla"]}
                                                {:identifier ["age"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:integer [1]}]}]}]}]}]}]}))

;; type assignment

(deftest type-assignment
  (check-ast (parse "mary is a girl")
             {:root [{:block [{:expression
                               [{:type-assignment
                                 [{:assignee [{:scalar [{:identifier ["mary"]}]}]}
                                  {:is-a [:is-a]}
                                  {:identifier ["girl"]}]}]}]}]}))

;; type assignment to slot

(deftest test-type-assignment-to-slot
  (check-ast (parse "mary friend is a person")
             {:root [{:block [{:expression
                               [{:type-assignment
                                 [{:assignee [{:object
                                               [{:identifier ["mary"]}
                                                {:identifier ["friend"]}]}]}
                                  {:is-a [:is-a]}
                                  {:identifier ["person"]}]}]}]}]}))


;; integers

(deftest test-single-digit-integer
  (= (utils/extract (parse "mary is 1") [:c 0 :c 0 :c 0 :c 2 :c 0 :c 0 :c 0])
     1))

(deftest test-multiple-digit-integer
  (= (utils/extract (parse "mary is 12345") [:c 0 :c 0 :c 0 :c 2 :c 0 :c 0 :c 0])
     12345))

(deftest test-integer-cannot-start-with-zero
  (try
    (parse "mary is 02345")
    (is false) ;; shouldn't get called
    (catch Exception e
      (is (re-find #"Got lost at" (.getMessage e))))))

;; assignment to primitive variable

(deftest assignment-number
  (check-ast (parse "mary is 1")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["mary"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:integer [1]}]}]}]}]}]}]}))

(deftest assignment-identifier
  (check-ast (parse "isla is age")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["isla"]}]}]}
                                  {:is [:is]}
                                  {:value [{:identifier ["age"]}]}]}]}]}]}))

(deftest assignment-string
  (check-ast (parse "isla is 'cool'")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["isla"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:string ["cool"]}]}]}]}]}]}]}))

;; blocks

(deftest two-expression-block
  (check-ast (parse "isla is 1\nmary is 2")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["isla"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:integer [1]}]}]}]}]}
                              {:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["mary"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:integer [2]}]}]}]}]}]}]}))

(deftest three-expression-block
  (check-ast (parse "name is 'Isla'\nwrite 'la'\nwrite name")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["name"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:string ["Isla"]}]}]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:literal [{:string ["la"]}]}]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:identifier ["name"]}]}]}]}
                              ]}]}))

(deftest test-block-with-type-ass-and-value-ass
  (check-ast (parse "name is 'Isla'\nmary is a girl\nwrite name")
             {:root [{:block [{:expression
                               [{:value-assignment
                                 [{:assignee [{:scalar [{:identifier ["name"]}]}]}
                                  {:is [:is]}
                                  {:value [{:literal [{:string ["Isla"]}]}]}]}]}
                              {:expression
                               [{:type-assignment
                                 [{:assignee [{:scalar [{:identifier ["mary"]}]}]}
                                  {:is-a [:is-a]}
                                  {:identifier ["girl"]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:identifier ["name"]}]}]}]}
                              ]}]}))

;; invocation

(deftest invoke-fn-scalar-param
  (check-ast (parse "write 'isla'")
             {:root [{:block [{:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:literal [{:string ["isla"]}]}]}]}]}]}]}))

;; (deftest invoke-fn-object-attribute-param
;;   (check-ast (parse "write mary age")
;;              {:root [{:block [{:expression
;;                                [{:invocation
;;                                  [{:identifier ["write"]}
;;                                   {:value [{:literal [{:string ["isla"]}]}]}]}]}]}]}))

(deftest test-write-string-regression
  (check-ast (parse "write 'My name Isla'")
             {:root [{:block [{:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:literal [{:string ["My name Isla"]}]}]}]}]}]}]}))

;; lists

(deftest test-list-instantiation
  (let [expected-ast {:root [{:block [{:expression
                                      [{:type-assignment
                                        [{:assignee [{:scalar [{:identifier ["items"]}]}]}
                                         {:is-a [:is-a]}
                                         {:identifier ["list"]}]}]}]}]}]
    (check-ast (parse "items is a list") expected-ast)))

(deftest test-list-add
  (let [expected-ast {:root [{:block [{:expression
                                      [{:list-assignment
                                        [{:assignee [{:scalar [{:identifier ["items"]}]}]}
                                         {:list-operation [{:add [:add]}]}
                                         {:value [{:identifier ["sword"]}]}]}]}]}]}]
    (check-ast (parse "items add sword") expected-ast)))

(deftest test-list-remove
  (let [expected-ast {:root [{:block [{:expression
                                      [{:list-assignment
                                        [{:assignee [{:scalar [{:identifier ["items"]}]}]}
                                         {:list-operation [{:remove [:remove]}]}
                                         {:value [{:identifier ["sword"]}]}]}]}]}]}]
    (check-ast (parse "items remove sword") expected-ast)))
