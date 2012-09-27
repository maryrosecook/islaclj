(ns isla.test.library
  (:use [isla.interpreter])
  (:use [isla.parser])
  (:require [isla.library :as library])
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:require [mrc.utils :as utils]))

;; write

(deftest test-write-object-attributes
  (let [result (interpret (parse "jimmy is a giraffe\njimmy instrument is 'guitar'
                                  jimmy height is '5 metres'\nwrite jimmy"))]
    (is (= (:ret result)
           "a giraffe\n  height is 5 metres\n  instrument is guitar"))))

(deftest test-write-list
  (let [result (interpret (parse "jimmy is a giraffe\njimmy instrument is 'guitar'
                                  isla is a tiger\nisla instrument is 'drums'
                                  basket is a list\n
                                  add jimmy to basket\nadd isla to basket\nwrite basket"))]
    (is (= (:ret result)
           "a list:\n  a giraffe\n    instrument is guitar\n  a tiger\n    instrument is drums"))))

(deftest test-write-object-with-list
  (let [result (interpret (parse "isla is a person\nisla friends is a list\n
                                  jimmy is a giraffe\njimmy instrument is 'guitar'
                                  add jimmy to isla friends\nwrite isla"))]
    (is (= (:ret result)
           "a person\n  friends is a list:\n    a giraffe\n      instrument is guitar"))))
