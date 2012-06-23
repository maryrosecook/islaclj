(ns isla.user)

(defrecord Instrument [sound awesomeness])
(def instrument-defaults ["" ""])

(def types
  {
   "instrument" {:type isla.user.Instrument :defaults instrument-defaults}
   })
