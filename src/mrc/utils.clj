(ns mrc.utils)

(defn run-method [instance method args]
  (clojure.lang.Reflector/invokeInstanceMethod instance method (to-array args)))

(defn thr [pieces]
  (throw (Exception. (apply str pieces))))

(defn extract [obj route]
  (if-let [nxt (first route)]
    (let [unrolled-obj (if (seq? obj) (vec obj) obj)]
      (if (contains? unrolled-obj nxt)
        (extract (get unrolled-obj nxt) (rest route))
        (thr [unrolled-obj " does not have " nxt])))
    obj))