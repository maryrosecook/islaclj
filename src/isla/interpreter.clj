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
      (run-sequence content (get-initial-context)))))

(defmethod interpret :block [node context]
  (run-sequence (:content node) context))

(defmethod interpret :expression [node context]
  (interpret (first (:content node)) context))

(defmethod interpret :assignment [node context]
  (def content (:content node))
  (def identifier (interpret (first content) context))
  (def value (interpret (nth content 2) context))
  (let [new-context (conj context [identifier value])]
    new-context))

(defmethod interpret :invocation [node context]
  (def content (vec (:content node)))
  (def function (lookup (interpret (first content) context) context))
  (def param (interpret (nth content 1) context))
  (let [return-val (function context param)] ;; call fn
    (nreturn context return-val)))

(defmethod interpret :value [node context]
  (if (= :identifier (:tag (first-content node)))
    (lookup (first-content (first-content node)) context) ;; sub is identifier - lookup+return
    (interpret (first-content node) context)))

(defmethod interpret :identifier [node context]
  (first-content node))

(defmethod interpret :assignee [node context]
  (first-content node))

(defmethod interpret :integer [node context]
  (first-content node))

(defmethod interpret :string [node context]
  (str/replace (first-content node) "'" ""))


(defn nreturn
  ([context] {:context context :return nil})
  ([context return] {:context context :return return}))

(defn lookup [identifier context]
  (get context identifier))

(defn first-content [node]
  (first (:content node)))

(defn run-sequence [nodes context]
  (if (empty? nodes)
    context ;; return context
    ;; interpret next node, get back context, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) context))))