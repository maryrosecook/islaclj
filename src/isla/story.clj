(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.utils :as utils])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.library :as library]))

(declare types name-into-objs extract-by-class get-story-ctx)

(defprotocol Playable
  (move [this direction]))

(defrecord Story [rooms player]
  Playable
  (move [this direction]
    (println "move!")))

(defrecord Monster [name description])
(def monster-defaults ["" ""])

(defrecord Player [name description current-room])
(def player-defaults ["" "" 0])

(defrecord Room [name description order objects])
(def room-defaults ["" "" 0 []])

(defn init-story [story-str]
  (let [env (interpreter/interpret
             (parser/parse story-str)
             (library/get-initial-env types (get-story-ctx)))
        ctx (:ctx env)

        rooms (name-into-objs (extract-by-class ctx (:type (get types "room"))))
        player (val (first (extract-by-class ctx (:type (get types "_player")))))]
(defn get-story-ctx []
  {"my" (interpreter/instantiate-type (get types "_player"))})

    (Story. rooms player)))
;; keys+vals -> vals
(defn name-into-objs [objs]
  (map (fn [x] (assoc (val x) :name (key x)))
       objs))

(defn extract-by-class [ctx clazz]
  (filter
   (fn [x] (= clazz (class (val x))))
   ctx))

(def types
  {
   "monster" {:type isla.story.Monster :defaults monster-defaults}
   "room" {:type isla.story.Room :defaults room-defaults}
   "_player" {:type isla.story.Player :defaults player-defaults}
   })
