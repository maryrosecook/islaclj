(ns isla.story
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.parser :as parser])
  (:require [isla.interpreter :as interpreter])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils])
  (:require [isla.library :as library])
  (:require [isla.talk :as t]))

(declare types name-into-objs extract-by-class get-story-ctx tuples-to-hash resolve-
         extract-arguments creturn)

(defrecord Monster [name summary])
(defrecord Player [name summary room items])
(defrecord Flower [name summary])

(defprotocol QueryableRoom
  (connected-rooms [this story]))

(defprotocol QueryableStory
  (items [this])
  (item [this name]))

(defprotocol Playable
  (go [this arguments-str])
  (look [this arguments-str]))

(defrecord Room [name summary items exit]
  QueryableRoom
  (connected-rooms [this story]
    (let [this-as-exits (filter (fn [x] (= this (:exit x))) (vals (:rooms story)))]
      (if (not= :undefined (:exit this))
        (conj this-as-exits (:exit this))
        this-as-exits))))

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
    (let [arguments (extract-arguments arguments-str)
          connected-to-current-room (connected-rooms (:room player) this)]
      (if (or (not= "into" (first arguments)) (nil? (second arguments)))
        (creturn this (t/go-instructions connected-to-current-room))
        (let [name (second arguments)
              room (get rooms name)]
          (if (some #{name} (map (fn [x] (:name x))
                                 connected-to-current-room))
            (creturn
             (assoc-in this [:player :room] room)
             (t/room-intro room (connected-rooms room this)))
            (if (= name (:name (:room player)))
              (creturn this (t/room-already name))
              (creturn this (t/room-not-allowed name))))))))

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

        rooms (extract-by-class ctx isla.story.Room)
        player (val (first (extract-by-class ctx isla.story.Player)))]
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
  {"my" ((get types "_player"))})

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
   "monster" (fn [] (new isla.story.Monster "" ""))
   "room" (fn [] (new isla.story.Room "" "" [] :undefined))
   "flower" (fn [] (new isla.story.Flower "" ""))
   "_player" (fn [] (new isla.story.Player "" "" :undefined #{}))
   })
