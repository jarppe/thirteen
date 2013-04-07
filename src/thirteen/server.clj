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
            [slingshot.slingshot :refer [throw+]]
            [thirteen.util :as util]))

(defn commit [payload]
  (let [pusher (:pusher payload)
        user (:name pusher)
        email (:email pusher)
        branch (second (re-find #"^refs\/heads\/(.*)$" (:ref payload)))]
    (println (format "PUSH: branch=%s, user=%s (%s)" branch user email))
    (status 200 "ok")))

(defn github [request]
  (let [ip            (get-in request [:headers "x-real-ip"])
        from-github?  (util/github-ip? ip)
        payload       (get-in request [:params :payload])]
    (when-not from-github?
      (println "warn: attempted access from non-github address:" ip)
      (throw+ {:status 403}))
    (when-not payload
      (println "warn: missing payload")
      (throw+ {:status 400}))
    (commit (c/parse-string payload true))))

(def app-routes
  [(POST "/github/commit" [] github)
   (GET "/ping" [] (json "pong"))
   (route/not-found "not found dude")])

(def prod-app
  (-> app-routes
    nm/app-handler
    util/wrap-json
    util/wrap-sling))

(def dev-app
  (-> prod-app
    wrap-reload))

(defn run []
  (jetty/run-jetty (var dev-app) {:port 8080 :join? false}))
