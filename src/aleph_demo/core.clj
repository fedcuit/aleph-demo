(ns aleph-demo.core
  (:require [aleph.http :as http]
            [aleph-demo.handler :as handler]))

(def server-opts {:port 80})

(defn -main
  [& args]
  (http/start-server handler/app server-opts))