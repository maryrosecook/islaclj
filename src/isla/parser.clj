(ns isla.parser
  (:use [clojure.pprint])
  (:use [isla.lexer])
  (:require [clojure.string :as str]))

(defn nnode [tag data]
  {:tag tag :content data})

(declare parse alternatives is-type pattern-sequence
         -root -block -expression -assignment -invocation
         -nl -integer -is -string -assignee -value -identifier)

(defn parse [code]
  (-root (lex code)))

(defn alternatives [input types] ;; bit round the houses
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
      (-block left-tokens (conj collected expr))))) ;; add expr, continue collecting more

;; expressions

(defn -expression [tokens collected]
  (def expressions (alternatives tokens [-assignment -invocation]))
  (if (> (count expressions) 0)
    (let [{expr :expr left-tokens :left-tokens} (first expressions)]
      {:expr (nnode :expression [expr]) :left-tokens left-tokens}) ;; return expr
    {:expr nil :left-tokens tokens})) ;; no expr match - return

(defn -assignment [tokens]
  (let [nodes (pattern-sequence tokens [-assignee -is -value -nl] [])]
    (if (= (count nodes) 4)
      {:expr (nnode :assignment (take 3 nodes)) :left-tokens (nthrest tokens 4)}
      nil)))

(defn -invocation [tokens]
  (let [nodes (pattern-sequence tokens [-identifier -value -nl] [])]
    (if (= (count nodes) 3)
      {:expr (nnode :invocation (take 2 nodes)) :left-tokens (nthrest tokens 3)}
      nil)))

(defn pattern-sequence [tokens patterns collected]
  (if (and (> (count patterns) 0) ;; still patterns to test
           (>= (count tokens) (count patterns))) ;; enough tokens to satisfy pattern
    (if-let [node ((first patterns) (first tokens))] ;; pattern matches token
      (pattern-sequence (rest tokens) (rest patterns) (conj collected node))
      collected)
    collected))

;; only one token, for now
(defn pattern [input matcher tag & args]
  (if (is-type matcher input)
    (let [output (if (not= nil args)
                   ((first args) input)
                   [input])]
      (nnode tag output))
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

