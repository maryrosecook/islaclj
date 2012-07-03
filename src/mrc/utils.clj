(ns mrc.utils)

(defn run-method [instance method args]
  (clojure.lang.Reflector/invokeInstanceMethod instance method (to-array args)))

(defn thr [pieces]
  (throw (Exception. (apply str pieces))))
