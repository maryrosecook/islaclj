(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.utils :as utils])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.library :as library]))

(declare types name-into-objs extract-by-class get-story-ctx)

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

(defrecord Room [name summary items door])
(def room-defaults ["" "" [] :undefined])

(defn init-story [story-str]
  (let [env (interpreter/interpret
             (parser/parse story-str)
             (library/get-initial-env types (get-story-ctx)))
        ctx (:ctx env)

        rooms (name-into-objs (extract-by-class ctx (:type (get types "room"))))
        player (val (first (extract-by-class ctx (:type (get types "_player")))))]

    (Story. player rooms)))

(defn run-command [story command-str]
  (let [command (first (str/split command-str #" "))
        arguments-str (second (str/split command-str #" " 2))
        arguments-vec (if (nil? arguments-str) [nil] [arguments-str])]
    (clojure.lang.Reflector/invokeInstanceMethod story command (to-array arguments-vec))))

(defn get-story-ctx []
  {"my" (interpreter/instantiate-type (get types "_player"))})

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
