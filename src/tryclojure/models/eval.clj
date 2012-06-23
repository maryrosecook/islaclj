(ns tryclojure.models.eval
  (:require [clojail.testers :refer [secure-tester-without-def]]
            [clojail.core :refer [sandbox]]
            [clojure.stacktrace :refer [root-cause]]
            [noir.session :as session])

  (:use [clojure.pprint])
  (:use [isla.parser])
  (:use [isla.interpreter])
  (:require [isla.story :as story])
  (:require [isla.utils :as utils])
  (:require [isla.library :as library])

  (:import java.io.StringWriter
	   java.util.concurrent.TimeoutException))

(declare run repl)

(def repl-env (ref (library/get-initial-env)))

(defn eval-string [isla-expr]
  ;; (throw (Exception. (str form)))
  (with-open [out (StringWriter.)]
    {:expr isla-expr
     :result [out (run isla-expr)]}))

(defn eval-request [expr]
  (try
    (eval-string expr)
    (catch TimeoutException _
      {:error true :message "Execution Timed Out!"})
    (catch Exception e
      {:error true :message (str (root-cause e))})))


(defn run [code]
  (let [return
        (interpret (first (:content (first (:content (isla.parser/parse code)))))
                   (deref repl-env))]
    (println return)
    (dosync (ref-set repl-env return))
    (:ret return)))

(defn repl []
  (run (utils/take-input))
  (repl))
;; story

(defmulti run-story-command (fn [command expr] command))

(defmethod run-story-command "hear" [command expr]
  (println command)
  (println expr)
  (if-let [story-name (second (str/split expr #" "))]
    (let [file-path (str/lower-case (str "stories/" story-name ".is"))
          story-str (slurp file-path)]
      (println "wooot")
      (def story (story/init-story story-str))
      (println story)
      (pprint story)
      "Are you sitting comfortably? Then, we shall begin.")
    (throw (Exception. "You must specify the name of the story you want to load."))))

(defmethod run-story-command :default [command expr] ;; normal command
  (if (nil? story)
    (throw (Exception. "Type 'hear --story--' to begin."))
    (story/run-command story expr)))

;; utils
