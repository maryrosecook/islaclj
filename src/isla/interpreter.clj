(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:require [clojure.string :as str]))

(declare run-sequence first-content lookup nreturn instantiate-type)

(defmulti interpret (fn [& args] (:tag (first args))))

(defmethod interpret :root [node & args]
  (let [content (:content node)]
    (if (not= nil args)
      (run-sequence content (first args))
      (run-sequence content (get-initial-env)))))

(defmethod interpret :block [node env]
  (run-sequence (:content node) env))

(defmethod interpret :expression [node env]
  (interpret (first (:content node)) env))

(defmethod interpret :slot-assignment [node env]
  (let [content (:content node)
        assignee (interpret (first content) env)
        slot (keyword (interpret (second content) env))
        value (interpret (nth content 3) env)]
    (let [new-ctx (assoc (:ctx env)
                    assignee
                    (assoc (get (:ctx env) assignee) slot value))]
      (nreturn new-ctx))))

(defmethod interpret :type-assignment [node env]
  (let [content (:content node)
        identifier (interpret (first content) env)
        type-identifier (interpret (nth content 2) env)
        type-hash (get (:types (:ctx env)) type-identifier)]
    (if (nil? type-hash)
      (throw (Exception. (str "I do not know what a " type-identifier " is.")))
      (let [new-ctx (assoc (:ctx env) identifier (instantiate-type type-hash))]
        (nreturn new-ctx)))))

(defmethod interpret :assignment [node env]
  (def content (:content node))
  (def identifier (interpret (first content) env))
  (def value (interpret (nth content 2) env))
  (let [new-ctx (assoc (:ctx env) identifier value)]
    (nreturn new-ctx)))

(defmethod interpret :invocation [node env]
  (def content (vec (:content node)))
  (def function (lookup (interpret (first content) env) env))
  (def param (interpret (nth content 1) env))
  (let [return-val (function env param)] ;; call fn
    (nreturn (:ctx env) return-val)))

(defmethod interpret :value [node env]
  (if (= :identifier (:tag (first-content node)))
    (lookup (first-content (first-content node)) env) ;; sub is identifier - lookup+return
    (interpret (first-content node) env)))

(defmethod interpret :identifier [node _]
  (first-content node))

(defmethod interpret :assignee [node _]
  (first-content node))

(defmethod interpret :integer [node _]
  (first-content node))

(defmethod interpret :string [node _]
  (str/replace (first-content node) "'" ""))

(defn nreturn
  ([ctx] {:ctx ctx :ret nil})
  ([ctx return] {:ctx ctx :ret return}))

(defn remret [env]
  (assoc env :ret nil))

(defn lookup [identifier env]
  (get (:ctx env) identifier))

(defn first-content [node]
  (first (:content node)))

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

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) (remret env)))))
