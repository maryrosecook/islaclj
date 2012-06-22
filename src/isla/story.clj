(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.utils :as utils])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.library :as library]))

(declare types extract)

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

        rooms (extract ctx (:type (get types "room")))
        player (first (extract ctx (:type (get types "_player"))))]
(defn get-story-ctx []
  {"my" (interpreter/instantiate-type (get types "_player"))})

    (Story. rooms player)))

(defn extract [ctx clazz]
  (filter
   (fn [x] (= clazz (class x)))
   (vals ctx)))

(def types
  {
   "monster" {:type isla.story.Monster :defaults monster-defaults}
   "room" {:type isla.story.Room :defaults room-defaults}
   "_player" {:type isla.story.Player :defaults player-defaults}
   })
