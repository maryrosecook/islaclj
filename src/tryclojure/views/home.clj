(ns tryclojure.views.home
  (:require [noir.core :refer [defpartial defpage]]
            [noir.response :refer [redirect]]
            [hiccup.element :refer [javascript-tag link-to unordered-list]]
            [hiccup.page :refer [include-css include-js html5]]))

(defn root-html [mode title]
  (html5
   [:head
    (include-css "/resources/public/css/tryclojure.css")
    (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"
                "/resources/public/javascript/jquery-console/jquery.console.js"
                "/resources/public/javascript/tryclojure.js")
    [:title "Try Isla"]]
   [:body
    [:div#wrapper
     [:div#content
      [:div#header
       [:h1 "Try Isla"]]
      [:div#container
       [:div#console.console]]
      (javascript-tag
       (str "mode = '" mode "';"))]]]))

(defpage "/" []
  (redirect "/story"))

(defpage "/story" []
  (root-html "story" "Story time with Isla"))

(defpage "/code" []
  (root-html "isla" "Try Isla"))
