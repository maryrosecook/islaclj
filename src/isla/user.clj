(ns isla.user)

(deftype IslaList [vector]
  clojure.lang.IPersistentCollection
  (cons [this item]
    (if (and (map? item)
             (:ref item)
             (some #{item} vector))
      this
      (IslaList. (conj vector item))))
  (seq [_]
    (seq vector))
  (empty [_]
    (IslaList. []))
  (equiv [this o]
    (and (instance? (class this) o)
         (= vector (.vector o))))

  clojure.lang.ISeq
  (first [_] (first vector))
  (next [_] (next vector))
  (more [_] (rest vector))

  clojure.lang.IPersistentSet
  (disjoin [this k]
    (let [idx (.indexOf vector k)]
      (if (> idx -1)
        (IslaList. (vec (concat (subvec vector 0 idx)
                                (subvec vector (inc idx)))))
        this)))

  Object
  (toString [_]
    (str vector))

  clojure.lang.IMeta
  (meta [_]
    (meta vector))

  clojure.lang.IObj
  (withMeta [_ meta]
    (IslaList. (with-meta vector meta)))

  clojure.lang.Counted
  (count [_]
    (count vector)))


(defn isla-list [& items]
  (->IslaList (vec items)))

(def types
  {
   "list" isla-list
   "generic" (fn [] {})
   })
