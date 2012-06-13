(ns isla.test.interpreter
  (:use [isla.interpreter])
  (:use [isla.parser])
  (:use [clojure.test])
  (:use [clojure.pprint]))


(deftest integer-assignment
  (let [result (interpret (parse "isla is 1"))]
    (is (= (get result "isla")
           1))))

;; (deftest test-write-out
;;   (let [result (interpret (parse "write 1"))]
;;     (is (= (:return result)
;;            1))))