(ns isla.talk
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [mrc.utils :as utils]))

(declare list-rooms)

(defn room-intro [room connected-rooms]
  (str "You are in the " (:name room) ". "
       (:summary room) " "
       "You can see doors leading to " (list-rooms connected-rooms)))

(defn room-already [name] (str "You are already in the " name "."))
(defn room-not-allowed [name] (str "You cannot go into the " name "."))

(defmulti list-rooms (fn [rooms] (count rooms)))
(defmethod list-rooms 0 [rooms] "There is no way out of this room.")
(defmethod list-rooms 1 [rooms] (str "You can see a door to the " (:name (first rooms))))
(defmethod list-rooms :default [rooms]
  (let [all-room-list (reduce (fn [string x]
                                (str string ", the " (:name x)))
                              "" rooms)
        all-but-last-list (rest (butlast (str/split all-room-list #",")))
        final (str "You can see a door to"
                   (str/join "," all-but-last-list)
                   " and the " (:name (last rooms)) ".")]
  final))
