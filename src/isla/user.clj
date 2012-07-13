(ns isla.user)

(defrecord IList [list])
(def list-defaults [[]])

(def types
  {
   "instrument" {:type isla.user.Instrument :defaults instrument-defaults}
   "list" {:type isla.user.IList :defaults list-defaults}
   })
