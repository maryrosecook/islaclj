(ns isla.talk
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [mrc.utils :as utils]))

(defn room-intro [room]
  (str "You are in the " (:name room) ". "
       (:summary room)))

(defn room-already [name] (str "You are already in the " name "."))
(defn room-not-allowed [name] (str "You cannot go into the " name "."))
