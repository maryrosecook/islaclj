(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils])
  (:require [isla.library :as library]))

(declare types name-into-objs extract-by-class get-story-ctx tuples-to-hash resolve-
         extract-arguments creturn)

(defrecord Monster [name summary])
(def monster-defaults ["" ""])

(defrecord Player [name summary room])
(def player-defaults ["" "" :undefined])


(defprotocol QueryableRoom
  (connected-rooms [this story]))

(defprotocol QueryableStory
  (all-items [this])
  (item [this name]))

(defprotocol Playable
  (go [this arguments-str])
  (look [this arguments-str]))

(def room-defaults ["" "" [] :undefined])
(defrecord Room [name summary items exit]
  QueryableRoom
  (connected-rooms [this story]
    (set (conj (filter (fn [x] (= this (:exit x))) (:rooms story))
               (:exit this)))))

(defrecord Story [player rooms]
  QueryableStory
  (all-items [this]
    (concat (map (fn [x] (:items x)) rooms)))
  (item [this name]
    (if-let [item (first (filter (fn [y] (= name (:name y))) (all-items this)))]
      item ;; item getter untested because didn't have items when wrote it
      (if (> (.indexOf ["myself" "me"] name) -1)
        player
        nil)))

  Playable
  (go [this arguments-str]
    (let [arguments (extract-arguments arguments-str)]
      (if (= "into" (first arguments))
        (let [name (second arguments)]
          (if (some #{name} (map (fn [x] (:name x))
                                 (connected-rooms (:room player) this)))
            (creturn
             (assoc this :player (assoc player :room (get rooms name)))
             (str "You are in the " name))
            (creturn this (str "You cannot go into the " name)))))))
  (look [this arguments-str]
    (let [arguments (extract-arguments arguments-str)]
      (if (empty? arguments)
        (creturn this (:summary (get player :room)))
        (if (= "at" (first arguments))
          (if-let [item (item this (second arguments))]
            (creturn this (:summary item))))))))

(defn init-story [story-str]
  (let [raw-env (interpreter/interpret
                 (parser/parse story-str)
                 (library/get-initial-env types (get-story-ctx)))
        env (assoc raw-env :ctx (name-into-objs (:ctx raw-env)))
        ctx (interpreter/resolve- (:ctx env) env)

        rooms (extract-by-class ctx (:type (get types "room")))
        player (val (first (extract-by-class ctx (:type (get types "_player")))))]
    (Story. player rooms)))

(defn run-command [story command-str]
  (let [command (first (str/split command-str #" "))
        arguments-str (second (str/split command-str #" " 2))
        arguments-vec (if (nil? arguments-str) [nil] [arguments-str])]
    (utils/run-method story command arguments-vec)))

(defn extract-arguments [arguments]
  (if (nil? arguments)
    []
    (str/split arguments #" ")))

(defn get-story-ctx []
  {"my" (story-utils/instantiate-type (get types "_player"))})

(defn name-into-objs [objs]
  (tuples-to-hash (map (fn [{k 0 v 1}]
                         [k (if (and (contains? v :name) (str/blank? (:name v)))
                              (assoc v :name k)
                              v)])
                       objs)))

(defn extract-by-class [ctx clazz]
  (tuples-to-hash (filter
                   (fn [x] (= clazz (class (val x))))
                   ctx)))

(defn tuples-to-hash [seq-]
  (reduce (fn [hash el] (assoc hash (get el 0) (get el 1)))
          {}
          seq-))

(defn creturn
  ([story] {:sto story :out nil})
  ([story output] {:sto story :out output}))

(def types
  {
   "monster" {:type isla.story.Monster :defaults monster-defaults}
   "room" {:type isla.story.Room :defaults room-defaults}
   "_player" {:type isla.story.Player :defaults player-defaults}
   })
