(ns thirteen.server
  (:require [clojure.pprint :refer [pprint]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [noir.util.middleware :as nm]
            [noir.response :refer [status json]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [cheshire.core :as c]
            [thirteen.util :as util]))

(defn handle-commit [payload]
  (pprint payload)
  (status 200 "ok"))

(defn commit [request]
  (if-let [payload (:payload request)]
    (handle-commit (c/parse-string payload))
    (status 400 "bad request")))

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
