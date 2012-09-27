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
       [:div.help "
         <strong>Assign</strong> &nbsp; &nbsp; &nbsp; <code>age is '2'</code><br/>
         <strong>Make object</strong> &nbsp;<code>jimmy is a giraffe</code><br/>
         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<code>jimmy instrument is 'drum'</code><br/>
         <strong>Make list</strong> &nbsp; &nbsp;<code>band is a list</code><br/>
         <strong>Change list</strong> &nbsp;<code>add jimmy to band</code><br/>
         &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<code>remove jimmy from band</code><br/>
         <strong>Output</strong> &nbsp; &nbsp; &nbsp;&nbsp;<code>write band</code><br/>




       "]

       [:div#console.console]]
      (javascript-tag
       (str "mode = '" mode "';"))]]]
   (footer)))

(defn footer []
  [:div.footer "<a href='/'>home</a>&nbsp;
                <a href='/repl'>try isla</a>&nbsp;
                <a href='http://github.com/maryrosecook/isla'>github</a>&nbsp;
                <a href='http://maryrosecook.com'>mary rose cook</a>"])

(defpage "/repl" []
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
         [:div.story.story-code "
           <span class='identifier'>isla</span> <span class='keyword'>is a</span>
           <span class='type'>person</span><br/>
           <span class='identifier'>isla</span> <span class='identifier'>lunch</span>
           <span class='keyword'>is</span> <span class='string'>'Jelly Tots'</span><br/>
           <br/>
           <span class='identifier'>drum</span> <span class='keyword'>is a</span>
           <span class='type'>toy</span><br/>
           <br/>

           <span class='identifier'>isla</span> <span class='identifier'>toys</span>
           <span class='keyword'>is a</span> <span class='type'>list</span><br/>
           <span class='keyword'>add</span> <span class='identifier'>drum</span>
           <span class='keyword'>to</span>
           <span class='identifier'>isla</span> <span class='identifier'>toys</span><br/>
          "]
         ]]]
       (footer)

       (javascript-tag
        (str "
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-24453347-2']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();"))]]]))
