(ns tryclojure.views.home
  (:require [noir.core :refer [defpartial defpage]]
            [noir.response :refer [redirect]]
            [hiccup.element :refer [javascript-tag link-to unordered-list]]
            [hiccup.page :refer [include-css include-js html5]]))

(defn repl-html [mode title]
  (html5
   [:head
    (include-css "/resources/public/css/tryclojure.css")
    (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"
                "/resources/public/javascript/jquery-console/jquery.console.js"
                "/resources/public/javascript/tryclojure.js")
    [:title title]]
   [:body
    [:div#wrapper
     [:div#content
      [:div#header
       [:h1 title]]
      [:div#container
       [:div#console.console]]
      (javascript-tag
       (str "mode = '" mode "';"))]]]))

(defpage "/story" []
  (repl-html "story" "Story time with Isla"))

(defpage "/code" []
  (repl-html "isla" "Try Isla"))

(defpage "/" []
  (html5
   [:head
    (include-css "/resources/public/css/tryclojure.css")

    [:title "Isla, a programming language for young children"]]
   [:body
    [:div#wrapper
     [:div#content
      [:div#header
       [:h1 "Isla"]
       [:div#subtitle "A programming language for young children"]]
      [:div#container
       [:div.prose-holder
        [:div.prose
         [:p "Use Isla to write your own story:
          "]
         [:div.story.story-code "
           <span class='identifier'>my</span> <span class='identifier'>name</a>
           <span class='keyword'>is</a> <span class='string'>'Mary'</a><br/><br/>

           <span class='identifier'>my</a> <span class='identifier'>summary</a>
           <span class='keyword'>is</a> <span class='string'>'You are a boy.
           You have no shoes.'</a><br/><br/>

           <span class='identifier'>hallway</a> <span class='keyword'>is a</a>
           <span class='type'>room</a><br/><br/>

           <span class='identifier'>hallway</a> <span class='identifier'>summary</a>
           <span class='keyword'>is</a>
           <span class='string'>'You are in a hallway.  A candle burns on a table.  You can see a door.'</a>
          "]
         [:p "
           Then, play through your adventure:
          "]
         [:div.story.story-playthrough "
           <span class='command'>> play The Hallway</span>
           <div class='output'>Are you sitting comfortably? Then, we shall begin.</div>
           <span class='command'>> look</span>
           <div class='output'>You are in a hallway.  A candle burns in front of a mirror.
           You can see a door.</div>
           <span class='command'>> open door</span>
           <div class='output'>The door is locked.</div>
           <span class='command'>> look at table</span>
           <div class='output'>You find a key.</div>
          "]
         [:p "
           No public version, yet.  The
           <a href='http://github.com/maryrosecook/isla'>code</a> is on github.
          "]
         ]]
       [:div.footer "by <a href='http://maryrosecook.com'>mary rose cook</a>"]]]]]))

