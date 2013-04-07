(ns thirteen.server
  (:require [clojure.pprint :refer [pprint]]
            [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [noir.util.middleware :as nm]
            [noir.response :refer [status json]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [cheshire.core :as c]
            [thirteen.util :as util]))

(defn commit [payload]
  (println "SUCCESS:")
  (pprint payload)
  (status 200 "ok"))

(def app-routes
  [(POST "/github/commit" [payload] (when payload (commit (c/parse-string payload true))))
   (GET "/ping" [] (json "pong"))
   (route/not-found "not found dude")])

(def prod-app
  (-> app-routes
    nm/app-handler
    util/wrap-json))

(def dev-app
  (-> prod-app
    wrap-reload))

(defn run []
  (jetty/run-jetty (var dev-app) {:port 8080 :join? false}))
