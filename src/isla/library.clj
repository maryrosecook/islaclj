(ns isla.library
  (:use [clojure.pprint])
  (:require [clojure.string :as str])
  (:require [isla.user :as user])
  (:require [isla.story-utils :as story-utils]))

(defn list-to-line-str [list]
  (apply str (interpose "\n" list)))

(defn assemble-output [param inset]
  (let [next-inset (str inset "  ")]
    (cond (map? param)
          (list-to-line-str (cons
                             (str inset "a " (:type (meta param)))
                             (map (fn [x]
                                    (str inset "  " (name x) " is " (assemble-output
                                                                     (get param x)
                                                                     next-inset)))
                                  (keys param))))
        (set? param)
        (list-to-line-str (cons "a list:"
                                (map (fn [x]
                                       (assemble-output x next-inset))
                                     param)))
        :default     param)))

(defn lib-write [env param]
  (let [output (assemble-output param "")]
    (println output)
    output))

(defn get-initial-env [& args]
  (def extra-types (first args))
  (def initial-ctx (second args))

  (def isla-ctx
    {
     ;; fns
     "write" lib-write ;; add to context

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
