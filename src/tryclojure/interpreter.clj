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

(defmethod interpret :block [node ctx]
  (run-sequence (:content node) ctx))

(defmethod interpret :expression [node ctx]
  (interpret (first (:content node)) ctx))

(defmethod interpret :assignment [node ctx]
  (def content (:content node))
  (def identifier (interpret (first content) ctx))
  (def value (interpret (nth content 2) ctx))
  (let [new-ctx (conj ctx [identifier value])]
    new-ctx))

(defmethod interpret :invocation [node ctx]
  (throw (Exception. "here!"))

  (def content (vec (:content node)))
  (def function (lookup (interpret (first content) ctx) ctx))
  (def param (interpret (nth content 1) ctx))
  (let [return-val (function ctx param)] ;; call fn
    (nreturn ctx return-val)))

(defmethod interpret :value [node ctx]
  (if (= :identifier (:tag (first-content node)))
    (lookup (first-content (first-content node)) ctx) ;; sub is identifier - lookup+return
    (interpret (first-content node) ctx)))

(defmethod interpret :identifier [node ctx]
  (first-content node))

(defmethod interpret :assignee [node ctx]
  (first-content node))

(defmethod interpret :integer [node ctx]
  (first-content node))

(defmethod interpret :string [node ctx]
  (str/replace (first-content node) "'" ""))


(defn nreturn
  (pprint "return!")
  (pprint return)
  ([ctx] {:ctx ctx :return-val nil})
  ([ctx return] {:ctx ctx :ret return}))

(defn lookup [identifier ctx]
  (get ctx identifier))

(defn first-content [node]
  (first (:content node)))

(defn run-sequence [nodes ctx]
  (if (empty? nodes)
    ctx ;; return context
    ;; interpret next node, get back context, pass that and remaining nodes back around
    (run-sequence (rest nodes) (interpret (first nodes) ctx))))