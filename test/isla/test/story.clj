(ns isla.test.story
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:use [isla.story]))

;; story creation

(deftest test-room-creation
  (let [story-str (str "palace is a room")
        story (init-story story-str)]
    (is (= (get (:rooms story) "palace")
           (assoc (isla.interpreter/instantiate-type (get types "room"))
             :name "palace")))))

(deftest test-player-alteration
  (let [story-str (str "my name is 'mary'")
        story (init-story story-str)]
    (is (= (:player story)
           (assoc (isla.interpreter/instantiate-type (get types "_player"))
             :name "mary")))))

(deftest test-room-alteration
  (let [summary "The floors are made of marble."
        story-str (str "palace is a room
                        palace summary is '" summary "'")
        story (init-story story-str)]
    (is (= (get (:rooms story) "palace")
           (assoc (isla.interpreter/instantiate-type (get types "room"))
             :name "palace" :summary summary)))))

;; story telling

(deftest test-look-general
  (let [summary "The floors are made of marble."
        story-str (str "palace is a room
                        palace summary is '" summary "'
                        my room is palace")
        story (init-story story-str)]
    (is (= (run-command story "look")
           summary))))

(deftest test-look-at-myself
  (let [summary "You have no shoes on."
        story-str (str "my summary is '" summary "'")
        story (init-story story-str)]
    (is (= (run-command story "look at myself")
           summary))))
