(ns user
  (:require [aleph.http :as http]
            [aleph-demo.handler :as handler]
            [aleph-demo.core :as core]
            [clojure.tools.namespace.repl :refer [refresh]])
  (:import (java.io Closeable)))

(def server nil)

(defn- stop-server
  [server]
  (when server (.close ^Closeable server))
  )

(defn- stop
  []
  (alter-var-root #'server stop-server))

(defn- start-server
  [_]
  (http/start-server handler/app core/server-opts))

(defn- start
  []
  (alter-var-root #'server start-server))

(defn reload
  []
  (stop)
  (refresh :after 'user/start)
  )