(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:use [isla.user :only [isla-list]])
  (:require [clojure.string :as str])
  (:require [isla.story-utils :as story-utils])
  (:require [mrc.utils :as utils]))

(declare run-sequence resolve- nreturn
         friendly-class friendly-symbol assign evaluate-value
         instantiate-type)

(defmulti interpret (fn [node & [env]] (:tag node)))

(defmethod interpret :root [node & [env]]
  (let [content (:c node)]
    (run-sequence content (or env (get-initial-env)))))

(defmethod interpret :block [node env]
  (run-sequence (:c node) env))

(defmethod interpret :expression [node env]
  (interpret (utils/extract node [:c 0]) env))

(defmethod interpret :value-assignment [node env]
  (let [assignee (utils/extract node [:c 0])
        value-node (interpret (utils/extract node [:c 2]) env)
        value (if (contains? value-node :ref)
                {:ref (:ref value-node)}
                (:val value-node))
        new-ctx (assign (:ctx env) assignee value)]
    (nreturn new-ctx)))

(defmethod interpret :type-assignment [node env]
  (let [assignee (utils/extract node [:c 0])
        type-identifier (interpret (utils/extract node [:c 2]) env)
        type-fn (or (get-in env [:ctx :types type-identifier])
                    (get-in env [:ctx :types "generic"]))
        type (instantiate-type type-fn type-identifier assignee)
        new-ctx (assign (:ctx env) assignee type)]
    (nreturn new-ctx)))

(defmethod interpret :array-operation [node env]
  (let [assignee (utils/extract node [:c 3])
        ;; little hack - dive into assignee, get object/scalar node and evaluate value
        original-list (:val (evaluate-value (utils/extract assignee [:c 0]) env))]
    (if (nil? original-list)
      (let [ref (:ref (evaluate-value (utils/extract assignee [:c 0]) env))
            variable-str (if (coll? ref)
                           (str (first ref) " " (name (second ref)))
                           ref)]
        (utils/thr (str "I do not know of a list called " variable-str ".")))
      (let [operation (utils/extract node [:c 0 :c 0 :tag])
            item-eval (interpret (utils/extract node [:c 1]) env)
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
        param (resolve- (:val (interpret (utils/extract node [:c 1]) env)) env)
        return-val (function env param)] ;; call fn
    (nreturn (:ctx env) return-val)))

(defmethod interpret :value [node env]
  (evaluate-value (utils/extract node [:c 0]) env))

(defn instantiate-type [type-fn identifier assignee]
  (with-meta (type-fn) {:type identifier}))

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
  (utils/extract node [:c 0]))

(defmulti assign (fn [_ assignee-node _] (utils/extract assignee-node [:c 0 :tag])))

(defmethod assign :scalar [ctx assignee-node value]
  (assoc ctx (utils/extract assignee-node [:c 0 :c 0 :c 0]) value))

(defmethod assign :object [ctx assignee-node value]
  (let [object-name (utils/extract assignee-node [:c 0 :c 0 :c 0])
        slot-name-str (utils/extract assignee-node [:c 0 :c 1 :c 0])
        current-slot-value (get (get ctx object-name) (keyword slot-name-str))]
    (assoc-in ctx [object-name (keyword slot-name-str)] value)))

(defn nreturn
  ([ctx] {:ctx ctx :ret nil})
  ([ctx return] {:ctx ctx :ret return}))

(defn remret [env]
  (assoc env :ret nil))

(defn dispatch-resolution [item]
  (cond (instance? isla.user.IslaList item)
        :list

        (instance? java.util.Map item)
        (if (:ref item)
          :ref
          :map)))

;; does not handle circular references
(defmulti resolve- (fn [ctx env] (dispatch-resolution ctx)))

(defmethod resolve- :default [thing env] thing)

(defmethod resolve- :ref [{:keys [ref]} env]
  (resolve- (get (:ctx env) ref) env))

(defmethod resolve- :map [map env]
  (reduce merge ;; just a hash that is not a ref - dive down
          map
          (clojure.core/map (fn [[k v]] {k (resolve- v env)}) map)))

(defmethod resolve- :list [list env]
  (reduce conj (isla-list) (map (fn [e] (resolve- e env)) list)))

(defn friendly-class [clazz]
  (last (str/split (str clazz) #"\.")))

(defn friendly-symbol [simbol]
  (last (str/split (str simbol) #":")))

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (recur (rest nodes) (interpret (first nodes) (remret env)))))
