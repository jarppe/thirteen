(ns thirteen.util
  (:require [clojure.walk :refer [keywordize-keys]]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defn starts-with [^String s ^String prefix]
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

