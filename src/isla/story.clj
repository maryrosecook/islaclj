(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils])
  (:require [isla.library :as library]))

(declare types name-into-objs extract-by-class get-story-ctx seq-to-hash resolve-)

(defprotocol Queryable
  (get-all-items [this])
  (get-item [this name]))

(defprotocol Playable
  ;; (move [this arguments])
  (look [this arguments]))

(defrecord Story [player rooms]
  Queryable
  (get-all-items [this]
    (concat (map (fn [x] (:items x)) rooms)))
  (get-item [this name]
    (if-let [item (first (filter (fn [y] (= name (:name y))) (get-all-items this)))]
      item ;; item getter untested because didn't have items when wrote it
      (if (> (.indexOf ["myself" "me"] name) -1)
        player
        nil)))

  Playable
  ;; (move [this arguments]
  ;;   (println "move!"))
  (look [this arguments]
    (if (nil? arguments)
      (:summary (get player :room))
      (let [arguments-vec (str/split arguments #" ")]
        (if (= "at" (first arguments-vec))
          (if-let [item (get-item this (second arguments-vec))]
            (:summary item))
          nil)))))


(defrecord Monster [name summary])
(def monster-defaults ["" ""])

(defrecord Player [name summary room])
(def player-defaults ["" "" :undefined])

(defrecord Room [name summary items exit])
(def room-defaults ["" "" [] :undefined])

(defn init-story [story-str]
  (let [env (interpreter/interpret
             (parser/parse story-str)
             (library/get-initial-env types (get-story-ctx)))
        ctx (interpreter/resolve- (:ctx env) env)

        rooms (seq-to-hash (name-into-objs
                            (extract-by-class ctx (:type (get types "room")))) :name)
        player (val (first (extract-by-class ctx (:type (get types "_player")))))]
    (Story. player rooms)))

(defn run-command [story command-str]
  (let [command (first (str/split command-str #" "))
        arguments-str (second (str/split command-str #" " 2))
        arguments-vec (if (nil? arguments-str) [nil] [arguments-str])]
    (utils/run-method story command arguments-vec)))


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

(def types
  {
   "monster" {:type isla.story.Monster :defaults monster-defaults}
   "room" {:type isla.story.Room :defaults room-defaults}
   "_player" {:type isla.story.Player :defaults player-defaults}
   })
