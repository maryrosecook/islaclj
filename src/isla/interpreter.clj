(ns isla.interpreter
  (:use [clojure.pprint])
  (:use [isla.library])
  (:require [clojure.string :as str]))

(declare run-sequence first-content lookup nreturn)

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

(defn run-sequence [nodes env]
  (if (empty? nodes)
    env ;; return env
    ;; interpret next node, get back env, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) (remret env)))))
