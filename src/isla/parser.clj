(ns isla.parser
  (:use [clojure.pprint])
  (:use [isla.lexer])
  (:require [clojure.string :as str]))

(defn nnode [tag data]
  {:tag tag :content data})

(declare expression-types atom-types value-types
         parse matched-nodes is-type pattern-sequence
         -root -block -expression -atom -assignment
         -nl -integer -is -string -assignee -value)

(defn parse [code]
  (-root (lex code)))

(defn matched-nodes [input types] ;; bit round the houses
  (vec (map (fn [t] (t input)) ;; get nodes for each matching type
            (filter (fn [t] (not= nil (t input))) ;; get matching types
                    types))))

(defmulti is-type (fn [matcher token] (class matcher)))

(defmethod is-type clojure.lang.Keyword [symbol token]
  (= symbol token))

(defmethod is-type java.util.regex.Pattern [re token]
  (if (keyword? token)
    false
    (if (not= nil (re-matches re token))
      true
      false)))


(defn -root [tokens]
  (nnode :root [(-block tokens [])]))

(defn -block [tokens collected]
  (let [{expr :expr left-tokens :left-tokens} (-expression tokens [])]
    (if (= expr nil)
      (nnode :block collected) ;; no more exprs, return block
      (-block left-tokens (conj collected expr))))) ;; no more exprs, return block

;; expressions

(defn -expression [tokens collected]
  (def expressions (matched-nodes tokens expression-types)) ;; candidates
  (if (> (count expressions) 0)
    (let [{expr :expr left-tokens :left-tokens} (first expressions)]
      {:expr (nnode :expression [expr]) :left-tokens left-tokens}) ;; return expr
    {:expr nil :left-tokens tokens})) ;; no expr match - return

(defn -assignment [tokens]
  (if (> (count tokens) 3)
    (let [nodes (pattern-sequence tokens [-assignee -is -value -nl] [])]
      (if (= (count nodes) 4)
        {:expr (nnode :assignment (take 3 nodes)) :left-tokens (nthrest tokens 4)}
        nil))
    nil))

(defn pattern-sequence [tokens patterns collected]
  (if (> (count patterns) 0)
      (pattern-sequence (rest tokens) (rest patterns)
                        (conj collected ((first patterns) (first tokens))))
    collected))






;; atoms

(defn -atom [token]
  (def nodes (matched-nodes token atom-types))
  (if (> (count nodes) 0)
    (first nodes) ;; return first match
    (throw (Exception. (str "Could not identify: " token)))))

(defn -nl [token]
  (if (is-type :nl token)
    (nnode :nl [token])
    nil))

(defn -is [token]
  (if (is-type #"is" token)
    (nnode :is [:is])
    nil))

;; values

(defn -value [token]
  (def nodes (matched-nodes token value-types))
  (if (> (count nodes) 0)
    (nnode :value [(first nodes)])
    nil))

(defn -assignee [token]
  (if (is-type #"[A-Za-z]+" token)
    (nnode :assignee [token])
    nil))

(defn -identifier [token]
  (if (is-type #"(?!^is$)[A-Za-z]+" token)
    (nnode :identifier [token])
    nil))

(defn -integer [token]
  (if (is-type #"[0-9]+" token)
    (nnode :integer [(Integer/parseInt token)])
    nil))

(defn -string [token]
  (if (is-type #"'[A-Za-z0-9 ]+'" token)
    (nnode :string [(str/replace token "'" "")])
    nil))


;; types

(def expression-types [-assignment])
(def atom-types [-nl])
(def value-types [-string -integer -identifier])