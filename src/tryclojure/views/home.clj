(ns tryclojure.views.home
  (:require [noir.core :refer [defpartial defpage]]
            [hiccup.element :refer [javascript-tag link-to unordered-list]]
            [hiccup.page :refer [include-css include-js html5]]))

(defpartial home-html []
  [:p.bottom
   ""])

(defn root-html []
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
       "var _gaq = _gaq || [];
        _gaq.push(['_setAccount', 'UA-27340918-1']);
        _gaq.push(['_trackPageview']);

        (function() {
          var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
          ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
          var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();")]]]))

(defpage "/" []
  (root-html))

(defpage "/story" []
  (root-html "story" "Story time with Isla"))

(defpage "/code" []
  (root-html "isla" "Try Isla"))
