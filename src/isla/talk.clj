(ns isla.talk
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [mrc.utils :as utils]))

(declare list-things)

;; look

(defn look-instructions [items-in-room]
  (if (> (count items-in-room) 0)
    (str "Try saying 'look at " (:name (first items-in-room)) "'.")
    (str "Try saying 'look' or 'look at myself'.")))

(defn look-not-here [name] (str "There is no " name " here."))

;; go

(defn go-instructions [connected-rooms]
  (if (> (count connected-rooms) 0)
    (str "Try saying 'go into " (:name (first connected-rooms)) "'.")
    (str "You cannot go anywhere.")))

;; pick up

(defn pick-up [name] (str "You have picked up the " name "."))

(defn pick-not-here [name] (str "There is no " name " here."))

(defn pick-instructions [items-in-room]
  (if (> (count items-in-room) 0)
    (str "Try saying 'pick up " (:name (first items-in-room)) "'.")
    (str "There is nothing to pick up here.")))

;; player description

(defn player-description [player]
  (str (:summary player) " "
       (list-things (:items player)
                    "You are not carrying anything."
                    "You are carrying"
                    "You are carrying"
                    "a")))

;; room description

(defn room-intro [room connected-rooms]
  (str "You are in the " (:name room) ". "
       (:summary room) " "
       (list-things (:items room)
                    "There are no items in this room."
                    "You can see "
                    "You can see "
                    "a") " "
       (list-things connected-rooms
                    "There is no way out of this room."
                    "You can see a door to"
                    "You can see doors to"
                    "the")))

(defn room-already [name] (str "You are already in the " name "."))
(defn room-not-allowed [name] (str "You cannot go into the " name "."))

(defmulti list-things (fn [things zero one-intro many-intro article] (count things)))
(defmethod list-things 0 [things zero one-intro many-intro article] zero)
(defmethod list-things 1 [things zero one-intro many-intro article]
  (str one-intro " " article " " (:name (first things)) "."))
(defmethod list-things :default [things zero one-intro many-intro article]
  (let [all-thing-list (reduce (fn [string x]
                                (str string ", " article " " (:name x)))
                              "" things)
        all-but-last-list (rest (butlast (str/split all-thing-list #",")))
        final (str many-intro
                   (str/join "," all-but-last-list)
                   " and " article " " (:name (last things)) ".")]
    final))
