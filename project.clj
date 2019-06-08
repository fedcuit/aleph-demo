(defproject aleph-demo "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [aleph "0.4.6"]
                 [org.clojure/core.async "0.4.490"]
                 [ring/ring-core "1.7.1"]
                 [funcool/promesa "1.9.0"]]
  :plugins [[lein-cljfmt "0.6.3"]]
  :main aleph-demo.core
  :profiles
  {:dev  {:dependencies [[javax.servlet/servlet-api "2.5"]
                         [ring/ring-mock "0.3.2"]
                         [org.clojure/tools.namespace "0.2.11"]]
          }
   :repl {:main user}})
