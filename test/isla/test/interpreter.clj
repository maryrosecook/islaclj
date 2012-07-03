(ns isla.test.interpreter
  (:use [isla.interpreter])
  (:use [isla.parser])
  (:use [isla.user])
  (:require [isla.library :as library])
  (:use [clojure.test])
  (:use [clojure.pprint]))

(def extra-types
  {"person"
   {:type (defrecord Person [age name friend])
    :defaults [0 "" :undefined]}})

;; non-slot assignment

(deftest integer-assignment
  (let [result (interpret (parse "isla is 1"))]
    (is (= (get (:ctx result) "isla")
           1))))

(deftest test-existing-obj-assignment-to-var
  (let [env (interpret (parse "isla is a person\nfriend is isla\nisla age is 1")
                       (library/get-initial-env extra-types))]
    (is (= (resolve- {:ref "friend"} env)
           (new isla.test.interpreter.Person 1 "" :undefined)))))


;; invocation

(deftest test-single-invoke-returns-return-value
  (let [result (interpret (parse "write 2"))]
    (is (= (:ret result) 2))))

(deftest test-next-expression-overwrites-ret-of-previous
  (let [result (interpret (parse "write 2\nwrite 3"))]
    (is (= (:ret result) 3))))

(deftest test-second-not-returning-expression-removes-ret-value-of-prev
  (let [result (interpret (parse "write 2"))]
    (is (= (:ret result) 2)) ;; check first would have ret val

    ;; run test
    (let [result (interpret (parse "write 2\nage is 1"))]
    (is (nil? (:ret result))))))

(deftest test-write-assigned-value
  (let [result (interpret (parse "name is 'mary'\nwrite name"))]
    (is (= (:ret result) "mary"))))

;; type assignment

(deftest test-type-assignment
  (let [result (interpret (parse "isla is a person")
                          (library/get-initial-env extra-types))]
    (is (= (get (:ctx result) "isla")
           (new isla.test.interpreter.Person 0 "" :undefined)))))

(deftest test-unknown-type-causes-exception
  (try
    (interpret (parse "isla is a giraffe")
               (library/get-initial-env extra-types))
    (is false) ;; should not get called
    (catch Exception e
      (is (= (.getMessage e) "I do not know what a giraffe is.")))))

;; slot assignment

(deftest test-slot-assignment
  (let [result (interpret (parse "isla is a person\nisla age is 1")
                          (library/get-initial-env extra-types))]
    (is (= (get (:ctx result) "isla")
           (new isla.test.interpreter.Person 1 "" :undefined)))))

(deftest test-slot-assignment-retention-of-other-slot-values
  (let [result (interpret (parse "isla is a person\nisla name is 'isla'\nisla age is 1")
                          (library/get-initial-env extra-types))]
    (is (= (get (:ctx result) "isla")
           (new isla.test.interpreter.Person 1 "isla" :undefined)))))

(deftest test-non-existent-slot-assignment
  (try
    (interpret (parse "isla is a person\nisla material is 'metal'")
               (library/get-initial-env extra-types))
    (is false) ;; shouldn't get called
    (catch Exception e
      (is (= (.getMessage e) "Persons do not have a material.")))))

(deftest test-slot-type-assignment
  (let [result (interpret (parse "isla is a person\nisla friend is a person")
                          (library/get-initial-env extra-types))]
    (is (= (get (:ctx result) "isla")
           (new isla.test.interpreter.Person 0
                ""
                (new isla.test.interpreter.Person 0 "" :undefined))))))

;; test extract fn

(deftest test-extract-block-tag
  (let [ast (parse "isla is a person")]
    (is (= (extract ast [:c 0 :tag]) :block))))

(deftest test-extract-way-deep-assignee-scalar-name
  (let [ast (parse "isla is a person")]
    (is (= (extract ast [:c 0 :c 0 :c 0
                         :c 0 :c 0 :c 0 :c 0]) "isla"))))

(deftest test-extract-way-deep-identifier-tag
  (let [ast (parse "isla is a person")]
    (is (= (extract ast [:c 0 :c 0 :c 0
                         :c 0 :c 0 :c 0 :tag]) :identifier))))
