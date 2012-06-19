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

;; slot assignment

(deftest slot-assignment
  (check-ast (parse "isla is a person\nisla age is 1")
             {:root [{:block [{:expression
                               [{:type-assignment
                                 [{:assignee ["isla"]}
                                  {:is-a [:is-a]}
                                  {:identifier ["person"]}]}]}
                              {:expression
                               [{:slot-assignment
                                 [{:assignee ["isla"]}
                                  {:identifier ["age"]}
                                  {:is [:is]}
                                  {:value [{:integer [1]}]}]}]}]}]}))

;; type assignment

(deftest type-assignment
  (check-ast (parse "mary is a girl")
             {:root [{:block [{:expression
                               [{:type-assignment
                                 [{:assignee ["mary"]}
                                  {:is-a [:is-a]}
                                  {:identifier ["girl"]}]}]}]}]}))

;; assignment to primitive variable

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



(deftest three-expression-block
  (check-ast (parse "name is 'Isla'\nwrite 'la'\nwrite name")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["name"]}
                                  {:is [:is]}
                                  {:value [{:string ["Isla"]}]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:string ["la"]}]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:identifier ["name"]}]}]}]}
                              ]}]}))


(deftest test-block-with-type-ass-and-value-ass
  (check-ast (parse "name is 'Isla'\nmary is a girl\nwrite name")
             {:root [{:block [{:expression
                               [{:assignment
                                 [{:assignee ["name"]}
                                  {:is [:is]}
                                  {:value [{:string ["Isla"]}]}]}]}
                              {:expression
                               [{:type-assignment
                                 [{:assignee ["mary"]}
                                  {:is-a [:is-a]}
                                  {:identifier ["girl"]}]}]}
                              {:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:identifier ["name"]}]}]}]}
                              ]}]}))


;; invocation

(deftest invoke-fn-one-param
  (check-ast (parse "write 'isla'")
             {:root [{:block [{:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:string ["isla"]}]}]}]}]}]}))

(deftest test-write-string-regression
  (check-ast (parse "write 'My name is Isla'")
             {:root [{:block [{:expression
                               [{:invocation
                                 [{:identifier ["write"]}
                                  {:value [{:string ["My name is Isla"]}]}]}]}]}]}))
