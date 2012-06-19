(ns isla.library
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.user :as user])
  (:require [isla.utils :as utils]))

(defn get-initial-env [& args]
  (def extra-types (first args))
  {
   :ret nil
   :ctx {
         ;; fns
         "write" (fn [env str]
                   (utils/output str) ;; print out
                   str) ;; add to context


         ;; types
         :types (if (nil? extra-types)
                  user/types
                  (merge extra-types user/types))
         }
  })
