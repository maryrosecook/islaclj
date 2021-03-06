(ns isla.story
  (:use [clojure.pprint])
  (:use [isla.user :only [isla-list]])
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
  (connected-rooms [this story])
  (item [this name]))

(defprotocol QueryableStory
  (items [this]))

(defprotocol Playable
  (go [this arguments-str])
  (look [this arguments-str])
  (pick [this arguments-str]))

(defrecord Room [name summary items exit]
  QueryableRoom
  (connected-rooms [this story]
    (let [this-as-exits (filter (fn [x] (= this (:exit x))) (vals (:rooms story)))]
      (if (not= :undefined (:exit this))
        (conj this-as-exits (:exit this))
        this-as-exits)))
  (item [this name]
    (first (filter (fn [x] (= (:name x) name)) items))))

(defrecord Story [player rooms]
  QueryableStory
  (items [this]
    (concat (map :items rooms)))

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
        (let [room (-> player :room)]
          (creturn this (t/room-intro room (connected-rooms room this))))
        (if (or (not= "at" (first arguments)) (nil? (second arguments)))
          (creturn this (t/look-instructions (-> player :room :items)))
          (let [name (second arguments)]
            (if-let [description (if (> (.indexOf ["myself" "me"] name) -1)
                                   (t/player-description player)
                                   (:summary (item (:room player) name)))]
              (creturn this description)
              (creturn this (t/look-not-here name))))))))

  (pick [this arguments-str]
    (let [arguments (extract-arguments arguments-str)
          items-in-room (-> player :room :items)]
      (if (or (not= "up" (first arguments)) (nil? (second arguments)))
        (creturn this (t/pick-instructions items-in-room))
        (let [name (second arguments)]
          (if (some #{name} (map (fn [x] (:name x))
                                 items-in-room))
            (let [item (item (:room player) name)]
              (creturn
               (assoc-in
                (assoc-in this [:player :items] (conj (:items player) item)) ;; item to player
                [:rooms (:name (:room player)) :items]
                (disj (-> player :room :items) item)) ;; room items w/o picked up item
               (t/pick-up name)))
            (creturn this (t/pick-not-here name)))))))
  )

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
  (into {} (map (fn [[k v]]
                  (let [v (if (and (contains? v :name) (str/blank? (:name v)))
                            (assoc v :name k)
                            v)]
                    [k v]))
                objs)))

(defn extract-by-class [ctx clazz]
  (into {} (filter (fn [x] (= clazz (class (val x))))
                   ctx)))

(defn creturn
  ([story] {:sto story :out nil})
  ([story output] {:sto story :out output}))

(def types
  {
   "monster" (fn [] (new isla.story.Monster "" ""))
   "room" (fn [] (new isla.story.Room "" "" (isla-list) :undefined))
   "flower" (fn [] (new isla.story.Flower "" ""))
   "_player" (fn [] (new isla.story.Player "" "" :undefined (isla-list)))
   })
