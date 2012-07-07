(ns isla.user)

(defrecord Instrument [sound awesomeness])
(def instrument-defaults ["" ""])

(defrecord IList [initial-list])
(def list-defaults [[]])

(def types
  {
   "instrument" {:type isla.user.Instrument :defaults instrument-defaults}
   "list" {:type isla.user.IList :defaults list-defaults}
   })
