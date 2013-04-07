(ns thirteen.server
  (:require [clojure.pprint :refer [pprint]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [noir.util.middleware :as nm]
            [noir.response :refer [status json]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [thirteen.util :as util]))

(defn commit [request]
  (println "*** COMMIT: ***")
  (pprint (:json request))
  (status 200 "ok"))

(def app-routes
  [(POST "/github/commit" [] commit)
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
