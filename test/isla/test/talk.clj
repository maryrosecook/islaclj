(ns isla.test.talk
  (:use [isla.talk])
  (:use [clojure.test])
  (:use [clojure.pprint]))

;; list-things

(def list-rooms-texts ["no way out of this room"
                       "you can see a door to" "you can see doors to" "the"])

(deftest test-0-rooms
  (is (re-find #"no way out of this room"
               (apply list-things (cons [] list-rooms-texts)))))

(deftest test-1-room
  (is (re-find #"see a door to the palace"
               (apply list-things (cons [{:name "palace"}] list-rooms-texts)))))

(deftest test-1-room
  (is (re-find #"see a door to the palace and the garden"
               (apply list-things (cons [{:name "palace"} {:name "garden"}]
                                        list-rooms-texts)))))


(deftest test-1-room
  (is (re-find #"see doors to the palace, the garden and the bedroom"
               (apply list-things (cons [{:name "palace"} {:name "garden"} {:name "bedroom"}]
                                        list-rooms-texts)))))

;; player

(deftest test-player-description
  (is (re-find #"you look like a girl You are carrying a flower."
               (player-description {:summary "you look like a girl"
                                    :items #{{:name "flower"}}}))))
