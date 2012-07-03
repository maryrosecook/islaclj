(ns isla.library
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.user :as user])
  (:require [isla.story-utils :as story-utils]))

(defn get-initial-env [& args]
  (def extra-types (first args))
  (def initial-ctx (second args))

  (def isla-ctx
    {
     ;; fns
     "write" (fn [env str]
               (story-utils/output str) ;; print out
               str) ;; add to context


     ;; types
     :types (if (nil? extra-types)
              user/types
              (merge extra-types user/types))
     })

  ;; final env
  {
   :ret nil
   :ctx (merge isla-ctx initial-ctx)
   })

