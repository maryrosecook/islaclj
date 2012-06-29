(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:require [clojure.string :as str]))

(declare run-sequence lookup nreturn instantiate-type
         friendly-class friendly-symbol assign extract thr)

(defmulti interpret (fn [& args] (:tag (first args))))

(defmethod interpret :root [node & args]
  (let [content (:content node)]
    (if (not= nil args)
      (run-sequence content (first args))
      (run-sequence content (get-initial-env)))))

(defmethod interpret :block [node env]
  (run-sequence (:content node) env))

(defmethod interpret :expression [node env]
  (interpret (extract node [:content 0]) env))

(defmethod interpret :value-assignment [node env]
  (let [assignee (extract node [:content 0])
        value (interpret (extract node [:content 2]) env)]
    (let [new-ctx (assign (:ctx env) assignee value)]
      (nreturn new-ctx))))

(defmethod interpret :type-assignment [node env]
  (let [assignee (extract node [:content 0])
        type-identifier (interpret (extract node [:content 2]) env)]
    (if-let [type-hash (get (:types (:ctx env)) type-identifier)]
      (let [new-ctx (assign (:ctx env) assignee (instantiate-type type-hash))]
        (nreturn new-ctx))
      (throw (Exception. (str "I do not know what a " type-identifier " is."))))))

(defmethod interpret :invocation [node env]
  (let [function (lookup (interpret (extract node [:content 0]) env) env)
        param (interpret (extract node [:content 1]) env)]
    (let [return-val (function env param)] ;; call fn
      (nreturn (:ctx env) return-val))))

(defmethod interpret :value [node env]
  (if (= :identifier (:tag (extract node [:content 0])))
    (lookup (extract node [:content 0 :content 0]) env) ;; sub is identifier - lookup+return
    (interpret (extract node [:content 0]) env)))

(defmethod interpret :identifier [node _]
  (extract node [:content 0]))

(defmethod interpret :assignee [node _]
  (extract node [:content 0]))

(defmethod interpret :integer [node _]
  (extract node [:content 0]))

(defmethod interpret :string [node _]
  (str/replace (extract node [:content 0]) "'" ""))

(defmulti assign (fn [_ assignee-node _] (extract assignee-node [:content 0 :tag])))

(defmethod assign :assignee-scalar [ctx assignee-node value]
  (assoc ctx (extract assignee-node [:content 0 :content 0 :content 0]) value))

(defmethod assign :assignee-object [ctx assignee-node value]
  (let [object-name (extract assignee-node [:content 0 :content 0 :content 0])
        slot-name-str (extract assignee-node [:content 0 :content 1 :content 0])
        current-slot-value (get (get ctx object-name) (keyword slot-name-str))]
    (if (nil? current-slot-value) ;; initial value of intended slot will never be nil
      (let [object-class (friendly-class (class (get ctx object-name)))]
        (throw (Exception. (str object-class "s do not have a " slot-name-str "."))))
      (assoc ctx object-name (assoc (get ctx object-name) (keyword slot-name-str) value)))))

(defn nreturn
  ([ctx] {:ctx ctx :ret nil})
  ([ctx return] {:ctx ctx :ret return}))

(defn remret [env]
  (assoc env :ret nil))

(defn lookup [identifier env]
  (get (:ctx env) identifier))

(defn extract [ast route]
  (if-let [nxt (first route)]
    (let [unrolled-ast (if (seq? ast) (vec ast) ast)]
      (if (contains? unrolled-ast nxt)
        (extract (get unrolled-ast nxt) (rest route))
        (thr ["AST " unrolled-ast " does not have " nxt])))
    ast))

(defn instantiate-type [type-hash]
  (clojure.lang.Reflector/invokeConstructor
   (:type type-hash)
   (to-array (:defaults type-hash))))

(defn thr [pieces]
  (throw (Exception. (apply str pieces))))

(defn friendly-class [clazz]
  (last (str/split (str clazz) #"\.")))

(defn friendly-symbol [simbol]
  (last (str/split (str simbol) #":")))

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) (remret env)))))
