(ns isla.test.talk
  (:use [isla.talk])
  (:use [clojure.test])
  (:use [clojure.pprint]))

;; list-rooms

(deftest test-0-rooms
  (is (re-find #"no way out of this room"
               (list-rooms []))))

(deftest test-1-room
  (is (re-find #"see a door to the palace"
               (list-rooms [{:name "palace"}]))))

(deftest test-2-rooms
  (is (re-find #"see a door to the palace and the garden"
               (list-rooms [{:name "palace"} {:name "garden"}]))))

(deftest test-3-rooms
  (is (re-find #"see a door to the palace, the garden and the bedroom."
               (list-rooms [{:name "palace"} {:name "garden"} {:name "bedroom"}]))))