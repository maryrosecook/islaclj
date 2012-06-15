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
  (if-let [{node :node left-tokens :left-tokens}
           (-expression tokens [])]
    (-block left-tokens (conj collected node)) ;; add expr, continue collecting more
    (nnode :block collected))) ;; no more exprs, return block

;; expressions

(defn -expression [tokens collected]
  (pattern-sequence-selector tokens [-assignment -invocation]))

(defn -assignment [tokens]
  (if-let [{nodes :nodes left-tokens :left-tokens}
           (pattern-sequence tokens [-assignee -is -value -nl] [])]
    {:node (nnode :assignment (take 3 nodes)) :left-tokens left-tokens}
    nil))

(defn -invocation [tokens]
  (if-let [{nodes :nodes left-tokens :left-tokens}
           (pattern-sequence tokens [-identifier -value -nl] [])]
    {:node (nnode :invocation (take 2 nodes)) :left-tokens left-tokens}
    nil))

;; atoms

(defn -nl [tokens] (pattern tokens :nl :nl))

(defn -is [tokens]
  (pattern tokens #"is" :is (fn [x] [:is])))

;; values

;;;;;;;; might need attention - doesn't get back left-tokens
(defn -value [tokens]
  (def alts (alternatives tokens [-string -integer -identifier]))
  (if (> (count alts) 0)
    (let [{node :node left-tokens :left-tokens} (first alts)]
      {:node (nnode :value [node]) :left-tokens left-tokens})
    nil))

(defn -assignee [tokens] (pattern tokens #"[A-Za-z]+" :assignee))

(defn -identifier [tokens] (pattern tokens #"(?!^is$)[A-Za-z]+" :identifier))

(defn -integer [tokens]
  (if (is-type #"[0-9]+" (first tokens))
    {:node (nnode :integer [(Integer/parseInt (first tokens))]) :left-tokens (rest tokens)}))

(defn -string [tokens]
  (pattern tokens #"'[A-Za-z0-9 ]+'" :string (fn [x] [(str/replace (first tokens) "'" "")])))

;; helpers

(defmulti is-type (fn [matcher tokens] (class matcher)))

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
    (let [{node :node left-tokens :left-tokens} (first alts)]
      {:node (nnode :expression [node]) :left-tokens left-tokens}) ;; return alternative
    nil)) ;; no alternatives match - return

(defn pattern-sequence [tokens patterns collected]
  (let [pattern (first patterns)
        token (first tokens)] ;;;;;;;;;;;;;;;;; need to fix - sub patterns shd manage own toks
    (if (nil? pattern)
      {:nodes collected :left-tokens tokens} ;; pattern matched - return
      (if (nil? token) ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ditto
        nil ;; pattern match failure - not enough tokens
        (if-let [{node :node left-tokens :left-tokens} (pattern tokens)] ;; matched token
          (pattern-sequence left-tokens (rest patterns) (conj collected node)) ;; round again
          nil))))) ;; pattern match failure token match failure - return

(defn pattern [tokens matcher tag & args]
  (if (is-type matcher (first tokens))
    (let [output (if (not= nil args)
                   ((first args) (first tokens))
                   [(first tokens)])]
      {:node (nnode tag output) :left-tokens (rest tokens)})
    nil))

(defn nnode [tag data]
  {:tag tag :content data})
