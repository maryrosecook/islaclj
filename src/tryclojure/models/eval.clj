(ns tryclojure.models.eval
  (:require [clojail.testers :refer [secure-tester-without-def]]
            [clojail.core :refer [sandbox]]
            [clojure.stacktrace :refer [root-cause]]
            [noir.session :as session])

  (:use [clojure.pprint])
  (:use [isla.parser])
  (:use [isla.interpreter])
  (:require [clojure.string :as str])
  (:require [isla.story :as story])
  (:require [isla.utils :as utils])
  (:require [isla.library :as library])

  (:import java.io.File)
  (:import java.io.StringWriter
	   java.util.concurrent.TimeoutException))

(declare run-isla-code run-story-command eval-expr)

(defn eval-request [{mode :mode expr :expr}]
  (try
    (with-open [out (StringWriter.)]
      {:expr expr
       :result [out (eval-expr mode expr)]})
    (catch TimeoutException _
      {:error true :message "Execution Timed Out!"})))
    ;; (catch Exception e
    ;;   {:error true :message (str (root-cause e))})))


(defmulti eval-expr (fn [mode _] mode))
(defmethod eval-expr "isla" [_ expr] (run-isla-code expr))
(defmethod eval-expr "story" [_ expr] (run-story-command (first (str/split expr #" ")) expr))

;; isla

(def isla-env (ref (library/get-initial-env)))

(defn run-isla-code [code]
  (let [return
        (interpret (first (:content (first (:content (isla.parser/parse code)))))
                   (deref isla-env))]
    (dosync (ref-set isla-env return))
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
