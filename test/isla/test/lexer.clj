(ns isla.test.lexer
  (:use [isla.lexer])
  (:use [clojure.test])
  (:use [clojure.pprint]))

(deftest nl-required
  (is (= (lex "1") ["1" :nl])))

(deftest no-nl-required
  (is (= (lex "1\n") ["1" :nl])))

(deftest assignment
  (is (= (lex "la is 1") ["la" "is" "1" :nl])))

(deftest two-assignments
  (is (= (lex "la is 1\nla is 1") ["la" "is" "1" :nl "la" "is" "1" :nl])))

(deftest extra-whitespace
  (is (= (lex "   la    is   1  \n la   is    1  ") ["la" "is" "1" :nl "la" "is" "1" :nl])))

(deftest test-multiple-newlines
  (is (= (lex "la is 1\n\n\n la is 2") ["la" "is" "1" :nl "la" "is" "2" :nl])))

(deftest test-keep-strings-intact
  (is (= (lex "name is 'isla eve'") ["name" "is" "'isla eve'" :nl])))
