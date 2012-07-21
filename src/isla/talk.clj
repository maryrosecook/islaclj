(ns isla.talk
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [mrc.utils :as utils]))

(declare list-rooms)

;; go

(defn go-instructions [connected-rooms]
  (if (> (count connected-rooms) 0)
    (str "Try saying 'go into " (:name (first connected-rooms)) "'.")
    (str "You cannot go anywhere.")))

;; pick up

(defn pick-up [name] (str "You have picked up the " name))

(defn pick-not-here [name] (str "There is no " name " here."))

(defn pick-instructions [items-in-room]
  (if (> (count items-in-room) 0)
    (str "Try saying 'pick up " (:name (first items-in-room)) "'.")
    (str "There is nothing to pick up here.")))

;; room description

(defn room-intro [room connected-rooms]
  (str "You are in the " (:name room) ". "
       (:summary room) " "
       (list-rooms connected-rooms)))

(defn room-already [name] (str "You are already in the " name "."))
(defn room-not-allowed [name] (str "You cannot go into the " name "."))

(defmulti list-rooms (fn [rooms] (count rooms)))
(defmethod list-rooms 0 [rooms] "There is no way out of this room.")
(defmethod list-rooms 1 [rooms] (str "You can see a door to the " (:name (first rooms)) "."))
(defmethod list-rooms :default [rooms]
  (let [all-room-list (reduce (fn [string x]
                                (str string ", the " (:name x)))
                              "" rooms)
        all-but-last-list (rest (butlast (str/split all-room-list #",")))
        final (str "You can see a door to"
                   (str/join "," all-but-last-list)
                   " and the " (:name (last rooms)) ".")]
  final))
