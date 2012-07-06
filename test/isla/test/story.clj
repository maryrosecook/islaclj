(ns isla.test.story
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:use [isla.story])
  (:require [isla.story-utils :as story-utils]))

(defn instantiate-with [type-name & args]
  (let [basic-obj (story-utils/instantiate-type (get types type-name))]
    (if (> (count args) 1)
      (apply assoc basic-obj args)
      basic-obj)))

;; story creation

(deftest test-room-creation
  (let [story-str (str "palace is a room")
        story (init-story story-str)]
    (is (= (get (:rooms story) "palace")
           (instantiate-with "room" :name "palace")))))

(deftest test-player-alteration
  (let [story-str (str "my name is 'mary'")
        story (init-story story-str)]
    (is (= (:player story)
           (instantiate-with "_player" :name "mary")))))

(deftest test-room-alteration
  (let [summary "The floors are made of marble."
        story-str (str "palace is a room
                        palace summary is '" summary "'")
        story (init-story story-str)]
    (is (= (get (:rooms story) "palace")
           (instantiate-with "room" :name "palace" :summary summary)))))

(deftest test-room-connection
  (let [story-str (str "palace is a room
                        garden is a room
                        palace exit is garden")
        story (init-story story-str)]
    (is (= (get (:rooms story) "palace")
           (instantiate-with "room" :name "palace"
                             :exit (instantiate-with "room" :name "garden"))))))

;; look

(deftest test-look-general
  (let [summary "The floors are made of marble."
        story-str (str "palace is a room
                        palace summary is '" summary "'
                        my room is palace")
        story (init-story story-str)]
    (is (= (:out (run-command story "look"))
           summary))))

(deftest test-look-at-myself
  (let [summary "You have no shoes on."
        story-str (str "my summary is '" summary "'")
        story (init-story story-str)]
    (is (= (:out (run-command story "look at myself"))
           summary))))

;; go

(deftest test-moving-between-rooms
  (let [story-str (str "palace is a room
                        garden is a room
                        palace exit is garden
                        my room is palace")
        story (init-story story-str)
        result (run-command story "go into garden")]
    (is (re-find #"You are in the garden" (:out result)))
    (is (:name (:room (:player (:sto result)))) "garden")))

(deftest test-trying-to-move-to-unconnected-room
  (let [story-str (str "palace is a room\ngarden is a room\nmy room is palace")
        story (init-story story-str)
        result (run-command story "go into garden")]
    (is (re-find #"You cannot go into the garden" (:out result)))
    (is (= story (:sto result)))))

(deftest test-trying-to-move-to-non-existent-room
  (let [story-str (str "palace is a room\nmy room is palace")
        story (init-story story-str)
        result (run-command story "go into garden")]
    (is (re-find #"You cannot go into the garden" (:out result)))
    (is (= story (:sto result)))))