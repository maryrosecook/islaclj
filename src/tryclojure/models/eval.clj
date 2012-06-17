(ns tryclojure.models.eval
  (:require [clojail.testers :refer [secure-tester-without-def]]
            [clojail.core :refer [sandbox]]
            [clojure.stacktrace :refer [root-cause]]
            [noir.session :as session])

  (:use [clojure.pprint])
  (:use [isla.parser])
  (:use [isla.interpreter])
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