(ns isla.parser
  (:use [clojure.pprint])
  (:use [isla.lexer])
  (:require [clojure.string :as str]))

(declare parse nnode
         alternatives is-type pattern-sequence pattern
         pattern-sequence-selector one-token-pattern
         -root -block -expression -type-assignment -value-assignment -invocation
         -nl -integer -is-a -is -string -assignee -value -identifier
         -scalar -object -variable -literal -add -remove -array-operation
         -list-operation)

(defn parse [code]
  (-root (lex code)))

(defn -root [tokens]
  (nnode :root [(-block tokens)]))

(defn -block
  ([tokens] (-block tokens []))
  ([tokens collected]
     (if-let [{node :node left-tokens :left-tokens}
              (-expression tokens)]
       (recur left-tokens (conj collected node)) ;; add expr, continue collecting more
       (if (= 0 (count tokens))
         (nnode :block collected) ;; no more exprs, all tokens used, return block
         (throw (Exception. (str "Got lost at: " (vec tokens)))))))) ;; tokens remaining - throw

;; expressions

(defn -expression
  ([tokens] (-expression tokens []))
  ([tokens collected]
     (pattern-sequence-selector tokens [-type-assignment -value-assignment
                                        -array-operation -invocation]
                                :expression)))

(defn -type-assignment [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-assignee -is-a -identifier -nl])]
    {:node (nnode :type-assignment (take 3 nodes)) :left-tokens left-tokens}))

(defn -value-assignment [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-assignee -is -value -nl])]
    {:node (nnode :value-assignment (take 3 nodes)) :left-tokens left-tokens}))

(defn -array-operation [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-assignee -list-operation -value -nl])]
    {:node (nnode :array-operation (take 3 nodes)) :left-tokens left-tokens}))

(defn -invocation [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-identifier -value -nl])]
    {:node (nnode :invocation (take 2 nodes)) :left-tokens left-tokens}))

(defn -list-operation [tokens]
  (when-let [{node :node left-tokens :left-tokens}
             (pattern-sequence-selector tokens [-add -remove] :list-operation)]
    {:node node :left-tokens left-tokens}))

;; atoms

(defn -is-a [tokens]
  (let [one (first tokens) two (second tokens)]
    (when (and (string? one)
               (string? two)
               (is-type #"is a" (str one " " two)))
      {:node (nnode :is-a [:is-a]) :left-tokens (nthrest tokens 2)})))

(defn -nl [tokens] (one-token-pattern tokens :nl :nl))

(defn -is [tokens]
  (one-token-pattern tokens #"is" :is (fn [x] [:is])))

(defn -add [tokens]
  (one-token-pattern tokens #"add" :add (fn [x] [:add])))

(defn -remove [tokens]
  (one-token-pattern tokens #"remove" :remove (fn [x] [:remove])))

;; values

(defn -identifier [tokens] (one-token-pattern tokens
                                              #"(?!^(is|add|remove)$)[A-Za-z]+"
                                              :identifier))

(defn -value [tokens]
  (pattern-sequence-selector tokens [-literal -variable] :value))

(defn -literal [tokens]
  (pattern-sequence-selector tokens [-integer -string] :literal))

(defn -variable [tokens]
  (pattern-sequence-selector tokens [-object -scalar] :variable))

(defn -assignee [tokens]
  (pattern-sequence-selector tokens [-object -scalar] :assignee))

(defn -scalar [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-identifier])]
    {:node (nnode :scalar (take 1 nodes)) :left-tokens left-tokens}))

(defn -object [tokens]
  (when-let [{nodes :nodes left-tokens :left-tokens}
             (pattern-sequence tokens [-identifier -identifier])]
    {:node (nnode :object (take 2 nodes)) :left-tokens left-tokens}))

(defn -integer [tokens]
  (one-token-pattern tokens #"[1-9][0-9]*" :integer
                     (fn [x] [(Integer/parseInt (first tokens))])))

(defn -string [tokens]
  (one-token-pattern tokens #"'[A-Za-z0-9 \.,\\]+'" :string
                     (fn [x] [(str/replace (first tokens) "'" "")])))

;; helpers

(defmulti is-type (fn [matcher tokens] (class matcher)))

(defmethod is-type clojure.lang.Keyword [symbol token]
  (= symbol token))

(defmethod is-type java.util.regex.Pattern [re token]
  (if (keyword? token) ;; looking for string but potential is kw so not a match
    false
    (not= nil (re-matches re token))))

(defn alternatives [input types]
  (vec (map (fn [t] (t input)) ;; get nodes for each matching type
            (filter (fn [t] (not= nil (t input))) ;; get matching types
                    types))))

(defn pattern-sequence-selector [tokens pattern-sequences tag]
  (let [alts (alternatives tokens pattern-sequences)]
    (when (> (count alts) 0)
      (let [{node :node left-tokens :left-tokens} (first alts)]
        {:node (nnode tag [node]) :left-tokens left-tokens})))) ;; return alternative

(defn pattern-sequence
  ([tokens patterns]
     (pattern-sequence tokens patterns []))
  ([tokens patterns collected]
     (if-let [pattern (first patterns)]
       (when-let [{node :node left-tokens :left-tokens} (pattern tokens)] ;; matched token
         (recur left-tokens (rest patterns) (conj collected node))) ;; round again
       {:nodes collected :left-tokens tokens}))) ;; pattern matched - return

(defn one-token-pattern [tokens matcher tag & [f]]
  (when-let [token (first tokens)]
    (when (is-type matcher token)
      (let [output (if f (f token) [token])]
        {:node (nnode tag output) :left-tokens (rest tokens)}))))

(defn nnode [tag data]
  {:tag tag :c data})
