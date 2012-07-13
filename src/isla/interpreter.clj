(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:require [clojure.string :as str])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils]))

(declare run-sequence resolve- nreturn
         friendly-class friendly-symbol assign)

(defmulti interpret (fn [& args] (:tag (first args))))

(defmethod interpret :root [node & args]
  (let [content (:c node)]
    (if (not= nil args)
      (run-sequence content (first args))
      (run-sequence content (get-initial-env)))))

(defmethod interpret :block [node env]
  (run-sequence (:c node) env))

(defmethod interpret :expression [node env]
  (interpret (utils/extract node [:c 0]) env))

(defmethod interpret :value-assignment [node env]
  (let [assignee (utils/extract node [:c 0])
        value-node (interpret (utils/extract node [:c 2]) env)
        value (if (contains? value-node :ref)
                {:ref (:ref value-node)}
                (:val value-node))]
    (let [new-ctx (assign (:ctx env) assignee value)]
      (nreturn new-ctx))))

(defmethod interpret :type-assignment [node env]
  (let [assignee (utils/extract node [:c 0])
        type-identifier (interpret (utils/extract node [:c 2]) env)]
    (if-let [type-fn (get (:types (:ctx env)) type-identifier)]
      (let [new-ctx (assign (:ctx env) assignee (type-fn))]
        (nreturn new-ctx))
      (utils/thr (str "I do not know what a " type-identifier " is.")))))

(defmethod interpret :list-assignment [node env]
  (let [assignee-name (utils/extract node [:c 0 :c 0 :c 0 :c 0])
        assignee (resolve- {:ref assignee-name} env)]
    (if (nil? assignee)
      (utils/thr (str "I do not know of a list called " assignee-name "."))
      (let [operation (utils/extract node [:c 1 :c 0 :tag])
            value (:val (interpret (utils/extract node [:c 2]) env))]
        (if (= :add operation)
          (assoc env :ctx (assoc (:ctx env) assignee-name (conj assignee value))))))))

(defmethod interpret :invocation [node env]
  (let [function (resolve- {:ref (interpret (utils/extract node [:c 0]) env)} env)
        param (:val (interpret (utils/extract node [:c 1]) env))]
    (let [return-val (function env param)] ;; call fn
      (nreturn (:ctx env) return-val))))

(defmethod interpret :value [node env]
  (let [sub-node (utils/extract node [:c 0])]
    (if (= :identifier (:tag sub-node))
      (let [ref (interpret sub-node env)]
        {:ref ref :val (resolve- {:ref ref} env)})
      {:val (interpret sub-node env)})))

(defmethod interpret :literal [node env]
  (interpret (utils/extract node [:c 0]) env))

(defmethod interpret :identifier [node _]
  (utils/extract node [:c 0]))

(defmethod interpret :assignee [node _]
  (utils/extract node [:c 0]))

(defmethod interpret :integer [node _]
  (utils/extract node [:c 0]))

(defmethod interpret :string [node _]
  (str/replace (utils/extract node [:c 0]) "'" ""))

(defmulti assign (fn [_ assignee-node _] (utils/extract assignee-node [:c 0 :tag])))

(defmethod assign :assignee-scalar [ctx assignee-node value]
  (assoc ctx (utils/extract assignee-node [:c 0 :c 0 :c 0]) value))

(defmethod assign :assignee-object [ctx assignee-node value]
  (let [object-name (utils/extract assignee-node [:c 0 :c 0 :c 0])
        slot-name-str (utils/extract assignee-node [:c 0 :c 1 :c 0])
        current-slot-value (get (get ctx object-name) (keyword slot-name-str))]
    (if (nil? current-slot-value) ;; initial value of intended slot will never be nil
      (let [object-class (friendly-class (class (get ctx object-name)))]
        (utils/thr (str object-class "s do not have a " slot-name-str ".")))
      (assoc ctx object-name (assoc (get ctx object-name) (keyword slot-name-str) value)))))

(defn nreturn
  ([ctx] {:ctx ctx :ret nil})
  ([ctx return] {:ctx ctx :ret return}))

(defn remret [env]
  (assoc env :ret nil))

;; does not handle circular references
(defmulti resolve- (fn [ast env] (class ast)))

(defmethod resolve- :default [thing env] thing)

(defmethod resolve- java.util.Map [ast env]
  (if (contains? ast :ref)
    (resolve- (get (:ctx env) (:ref ast)) env) ;; resolve and dive down
    (reduce (fn [hash el] ;; just dive down
              (merge hash el)) ast
              (map (fn [e] {(get e 0) (resolve- (get e 1) env)}) ast))))

(defn friendly-class [clazz]
  (last (str/split (str clazz) #"\.")))

(defn friendly-symbol [simbol]
  (last (str/split (str simbol) #":")))

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) (remret env)))))
