(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:require [clojure.string :as str])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils]))

(declare run-sequence resolve- nreturn
         friendly-class friendly-symbol assign evaluate-value)

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

(defmethod interpret :array-operation [node env]
  (let [assignee (utils/extract node [:c 0])
        ;; little hack - dive into assignee, get object/scalar node and evaluate value
        original-list (:val (evaluate-value (utils/extract assignee [:c 0]) env))]
    (if (nil? original-list)
      (let [ref (:ref (evaluate-value (utils/extract assignee [:c 0]) env))
            variable-str (if (coll? ref)
                           (str (first ref) " " (name (second ref)))
                           ref)]
        (utils/thr (str "I do not know of a list called " variable-str ".")))
      (let [operation (utils/extract node [:c 1 :c 0 :tag])
            item-eval (interpret (utils/extract node [:c 2]) env)
            item (if (contains? item-eval :ref)
                   {:ref (:ref item-eval)}
                   (:val item-eval))
            new-list (cond
                      (= :add operation) (conj original-list item)
                      (= :remove operation) (disj original-list item)
                      :else (utils/thr (str "You cannot " operation " on lists.")))
            new-ctx (assign (:ctx env) assignee new-list)]
        (nreturn new-ctx)))))

(defmethod interpret :invocation [node env]
  (let [function (resolve- {:ref (interpret (utils/extract node [:c 0]) env)} env)
        param (:val (interpret (utils/extract node [:c 1]) env))]
    (let [return-val (function env param)] ;; call fn
      (nreturn (:ctx env) return-val))))

(defmethod interpret :value [node env]
  (evaluate-value (utils/extract node [:c 0]) env))

(defmulti evaluate-value (fn [node _] (:tag node)))

(defmethod evaluate-value :literal [node env]
  {:val (interpret (utils/extract node [:c 0]) env)})

(defmethod evaluate-value :variable [node env]
  (evaluate-value (utils/extract node [:c 0]) env))

(defmethod evaluate-value :scalar [node env]
  (let [identifier (interpret (utils/extract node [:c 0]) env)]
    {:ref identifier :val (get (:ctx env) identifier)}))

(defmethod evaluate-value :object [node env]
  (let [object-identifier (utils/extract node [:c 0 :c 0])
        attribute-identifier (keyword (utils/extract node [:c 1 :c 0]))]
    {:ref [object-identifier attribute-identifier] ;; not used now,
                                                   ;; won't work if assign obj-attr to var
     :val (get (get (:ctx env) object-identifier)
               attribute-identifier)}))

(defmethod interpret :identifier [node _]
  (utils/extract node [:c 0]))

(defmethod interpret :integer [node _]
  (utils/extract node [:c 0]))

(defmethod interpret :string [node _]
  (str/replace (utils/extract node [:c 0]) "'" ""))

(defmulti assign (fn [_ assignee-node _] (utils/extract assignee-node [:c 0 :tag])))

(defmethod assign :scalar [ctx assignee-node value]
  (assoc ctx (utils/extract assignee-node [:c 0 :c 0 :c 0]) value))

(defmethod assign :object [ctx assignee-node value]
  (let [object-name (utils/extract assignee-node [:c 0 :c 0 :c 0])
        slot-name-str (utils/extract assignee-node [:c 0 :c 1 :c 0])
        current-slot-value (get (get ctx object-name) (keyword slot-name-str))]
    (if (nil? current-slot-value) ;; initial value of intended slot will never be nil
      (let [object-class (friendly-class (class (get ctx object-name)))]
        (utils/thr (str object-class "s do not have a " slot-name-str ".")))
      (assoc-in ctx [object-name (keyword slot-name-str)] value))))

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
    (resolve- (get (:ctx env) (:ref ast)) env) ;; got an actual ref - resolve it
    (reduce (fn [hash el] ;; just a hash that is not a ref - dive down
              (merge hash el)) ast
              (map (fn [e] {(get e 0) (resolve- (get e 1) env)}) ast))))

(defmethod resolve- clojure.lang.PersistentHashSet [ast env]
  (let [out (reduce (fn [set el]
                      (conj set el))
                    #{}
                    (map (fn [e] (resolve- e env)) ast))]
    out))

(defn friendly-class [clazz]
  (last (str/split (str clazz) #"\.")))

(defn friendly-symbol [simbol]
  (last (str/split (str simbol) #":")))

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) (remret env)))))
