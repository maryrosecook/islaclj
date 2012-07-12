(ns isla.cli
  (:use [isla.parser])
  (:use [isla.interpreter])
  (:require [isla.story-utils :as story-utils])
  (:require [isla.library :as library])
  (:use [clojure.pprint])
  (:import java.io.File)
  (:require [clojure.string :as str]))

(declare repl)

(def story-dir "stories/")

(defn run-story [story-name]
  (println "  -----------------------------------")
  (def file-path (str/lower-case (str story-dir story-name ".is")))
  ;; (pprint (parse (slurp file-path)))
  (let [context (interpret (parse (slurp file-path)))]
    context))

(defn get-stories []
  (.listFiles (File. story-dir)))

(defn write-out-stories []
  (doseq [file (get-stories)]
    (story-utils/output (str "  " (str/capitalize (first (str/split (.getName file) #"\.")))))))

(defn introduction []
  (println (str "
  My name is Isla.
  Here are the stories I know:\n"))

  (write-out-stories)

  (println (str "
  Which story would you like?\n")))


(defn -main [& args]
  (def repl-context (ref (library/get-initial-context)))

  (println)
  (story-utils/output "-------------------------------------------------------\n")
  (story-utils/output "Hello. My name is Isla. It's terribly nice to meet you.\n")

  (repl)

  (flush)) ;; empty return to stop last thing executed from being printed to console



(defn run [code]
  (dosync (ref-set repl-context (interpret (parse code) (deref repl-context)))))

(defn repl []
  (run (story-utils/take-input))
  (repl))

;; (defn -main [& args]
;;   (introduction)

;;   (run-story (story-utils/take-input))
;;   ;; (def first-story (first (str/split (.getName (first (get-stories))) #"\.")))
;;   ;; (run-story first-story)

;;   (flush)) ;; empty return to stop last thing executed from being printed to console
