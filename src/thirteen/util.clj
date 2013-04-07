(ns thirteen.util
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.java.io :as io]
            [noir.response :as resp]
            [cheshire.core :as json]
            [slingshot.slingshot :refer [throw+ try+]]))

(def github-addresses ["207.97.227.253/32"
                       "50.57.128.197/32"
                       "108.171.174.178/32"
                       "50.57.231.61/32"
                       "204.232.175.64/27"
                       "192.30.252.0/22"])

(defn- ->int [s]
  (Integer/parseInt s))

(defn- ->ip
  ([address]
    (apply ->ip (map ->int (drop 1 (re-find #"(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})" address)))))
  ([a1  a2 a3 a4]
    (bit-or (bit-shift-left a1 24)
            (bit-shift-left a2 16)
            (bit-shift-left a3 8)
            a4)))

(defn- ->mask [mask-len]
  (bit-and 0xFFFFFFFF (bit-shift-left 0xFFFFFFFF (- 32 mask-len))))

(defn- ->cidr [address]
  (let [[a1 a2 a3 a4 mask] (map ->int (drop 1 (re-find #"(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})/(\d{1,2})" address)))]
    [(->ip a1 a2 a3 a4) (bit-and 0xFFFFFFFF (bit-shift-left 0xFFFFFFFF (- 32 mask)))]))

(defn- cidr-match [source [target mask]]
  (= (bit-and source mask) target))

(def ^:private github-cidrs (map ->cidr github-addresses))

(defn github-ip? [address]
  (some (partial cidr-match (->ip address)) github-cidrs))

(defn starts-with [^String s prefix]
  (when (and s prefix)
    (.startsWith s prefix)))

(defn wrap-json [handler]
  (fn [request]
    (let [json-body (if (starts-with (:content-type request) "application/json")
                      (if-let [body (:body request)]
                        (-> body
                          (io/reader :encoding (or (:character-encoding request) "utf-8"))
                          json/parse-stream
                          keywordize-keys)
                        {}))
          request (assoc request :json json-body)
          request (if json-body (assoc request :params json-body) request)]
      (handler request))))

(defn wrap-sling [handler]
  (fn [request]
    (try+
      (handler request)
      (catch map? {:keys [status message]}
        (resp/status status (or message ""))))))

(try+
  (throw+ {:status "foo"})
  (catch map? {status :status message :message}
    (println "Ups:" status)))

