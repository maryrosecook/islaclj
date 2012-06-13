(ns isla.library
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.utils :as utils]))

(defn get-initial-context []
  :output []
  {
   "write" (fn [context str]
             (utils/output str) ;; print out
              str) ;; add to context
  })