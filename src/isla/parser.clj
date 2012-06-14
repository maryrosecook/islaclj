(ns isla.parser
  (:use [clojure.pprint])
  (:use [isla.lexer])
  (:require [clojure.string :as str]))

(declare parse alternatives is-type pattern-sequence pattern nnode pattern-sequence-selector
         -root -block -expression -assignment -invocation
         -nl -integer -is -string -assignee -value -identifier)

(defn parse [code]
  (-root (lex code)))

(defn -root [tokens]
  (nnode :root [(-block tokens [])]))

(defn -block [tokens collected]
  (if-let [{expr :expr left-tokens :left-tokens}
           (-expression tokens [])]
    (-block left-tokens (conj collected expr)) ;; add expr, continue collecting more
    (nnode :block collected))) ;; no more exprs, return block

;; expressions

(defn -expression [tokens collected]
  (pattern-sequence-selector tokens [-assignment -invocation]))

(defn -assignment [tokens]
  (if-let [{nodes :nodes left-tokens :left-tokens}
           (pattern-sequence tokens [-assignee -is -value -nl] [])]
    {:expr (nnode :assignment (take 3 nodes)) :left-tokens left-tokens}
    nil))

(defn -invocation [tokens]
  (if-let [{nodes :nodes left-tokens :left-tokens}
           (pattern-sequence tokens [-identifier -value -nl] [])]
    {:expr (nnode :invocation (take 2 nodes)) :left-tokens left-tokens}
    nil))

;; atoms

(defn -nl [token] (pattern token :nl :nl))

(defn -is [token]
  (pattern token #"is" :is (fn [x] [:is])))

;; values

(defn -value [token]
  (def nodes (alternatives token [-string -integer -identifier]))
  (if (> (count nodes) 0)
    (nnode :value [(first nodes)])
    nil))

(defn -assignee [token] (pattern token #"[A-Za-z]+" :assignee))

(defn -identifier [token] (pattern token #"(?!^is$)[A-Za-z]+" :identifier))

(defn -integer [token]
  (pattern token #"[0-9]+" :integer (fn [x] [(Integer/parseInt token)])))

(defn -string [token]
  (pattern token #"'[A-Za-z0-9 ]+'" :string (fn [x] [(str/replace token "'" "")])))

;; helpers

(defmulti is-type (fn [matcher token] (class matcher)))

(defmethod is-type clojure.lang.Keyword [symbol token]
  (= symbol token))

(defmethod is-type java.util.regex.Pattern [re token]
  (if (keyword? token)
    false
    (if (not= nil (re-matches re token))
      true
      false)))

(defn alternatives [input types]
  (vec (map (fn [t] (t input)) ;; get nodes for each matching type
            (filter (fn [t] (not= nil (t input))) ;; get matching types
                    types))))

(defn pattern-sequence-selector [tokens pattern-sequences]
  (def alts (alternatives tokens pattern-sequences))
  (if (> (count alts) 0)
    (let [{expr :expr left-tokens :left-tokens} (first alts)]
      {:expr (nnode :expression [expr]) :left-tokens left-tokens}) ;; return alternative
    nil)) ;; no alternative match - return

(defn pattern-sequence [tokens patterns collected]
  (let [pattern (first patterns)
        token (first tokens)]
    (if (nil? pattern)
      {:nodes collected :left-tokens tokens} ;; pattern matched - return
      (if (nil? token)
        nil ;; pattern match failure - not enough tokens
        (if-let [node (pattern token)] ;; matched token
          (pattern-sequence (rest tokens) (rest patterns) (conj collected node)) ;; round again
          nil))))) ;; pattern match failure token match failure - return

;; only one token, for now
(defn pattern [input matcher tag & args]
  (if (is-type matcher input)
    (let [output (if (not= nil args)
                   ((first args) input)
                   [input])]
      (nnode tag output))
    nil))

(defn nnode [tag data]
  {:tag tag :content data})
