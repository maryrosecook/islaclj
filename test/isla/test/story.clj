(ns isla.test.story
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:use [isla.story]))

;; story creation

(deftest test-room-creation
  (let [story-str (str "palace is a room")
        story (init-story story-str)]
    (is (= (first (:rooms story))
           (assoc (isla.interpreter/instantiate-type (get types "room"))
             :name "palace")))))

(deftest test-player-alteration
  (let [story-str (str "my name is 'mary'")
        story (init-story story-str)]
    (is (= (:player story)
           (assoc (isla.interpreter/instantiate-type (get types "_player"))
             :name "mary")))))

(deftest test-room-alteration
  (let [description "The floors are made of marble."
        story-str (str "palace is a room
                        palace description is '" description "'")
        story (init-story story-str)]
    (is (= (first (:rooms story))
           (assoc (isla.interpreter/instantiate-type (get types "room"))
             :name "palace" :description description)))))

;; story telling

(deftest test-look-general
  (let [description "The floors are made of marble."
        story-str (str "palace is a room
                        palace description is '" description "'")
        story (init-story story-str)]
    (is (= (run-command story "look")
           description))))
