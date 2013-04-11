(ns tryclojure.views.home
  (:require [noir.core :refer [defpartial defpage]]
            [noir.response :refer [redirect]]
            [hiccup.element :refer [javascript-tag link-to unordered-list]]
            [hiccup.page :refer [include-css include-js html5]]))

(defn footer []
  [:div.footer "<a href='/'>home</a>&nbsp;
                <a href='/repl'>try isla</a>&nbsp;
                <a href='http://github.com/maryrosecook/isla'>github</a>&nbsp;
                <a href='http://maryrosecook.com'>mary rose cook</a>"])

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

(defn redirect-to-new-site []
  (redirect "http://islalanguage.org"))

(defpage "/repl" []
  (redirect-to-new-site))

(defpage "/" []
  (redirect-to-new-site))
