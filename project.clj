(defproject thirteen "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.4.9"]
                 [compojure "1.1.5" :exclusions [ring/ring-core]]
                 [slingshot "0.10.3"]]
  :profiles {:prod {:ring {:handler thirteen.server/prod-app
                           :open-browser? false
                           :stacktraces? false
                           :auto-reload? false}}}
  :plugins [[lein-ring "0.8.3"]]
  :main thirteen.server
  :ring {:handler thirteen.server/dev-app
         :port 8080
         :open-browser? false}
  :repl-options {:init-ns thirteen.server}
  :min-lein-version "2.0.0")
