(ns aleph-demo.handler
  (:require
   [compojure.core :refer [GET]]
   [compojure.route :as route]
   [compojure.response :refer [Renderable]]
   [manifold.stream :as s]
   [manifold.deferred :as d]
   [clojure.core.async :as a]
   [clojure.java.io :refer [file]]
   [compojure.core :refer :all]
   [compojure.route :as route]
   [promesa.core :as p]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :refer [response]]))

(defn hello-world-handler
  [req] (response "Hello world"))

(defn delayed-hello-world-handler
  [req]
  (let [d (d/deferred)]
    (-> (p/delay 1000 (response "Hello world"))
        (p/branch #(d/success! d %) #(d/error! d %)))
    d))

(extend-protocol Renderable
  manifold.deferred.IDeferred
  (render [d _] d))

(defn streaming-numbers-handler
  [{:keys [params]}]
  (let [cnt (Integer/parseInt (get params "count" "0"))]
    {:status  200
     :headers {"content-type" "text/plain"}
     :body    (let [sent (atom 0)]
                (->> (s/periodically 100 #(str (swap! sent inc) "\n"))
                     (s/transform (take cnt))))}))

(defn streaming-numbers-handler
  [{:keys [params]}]
  (let [cnt (Integer/parseInt (get params "count" "0"))]
    {:status  200
     :headers {"content-type" "text/plain"}
     :body    (->> (range cnt)
                   (map #(do (Thread/sleep 100) %))
                   (map #(str % "\n")))}))

(defn streaming-numbers-handler
  [{:keys [params]}]
  (let [cnt (Integer/parseInt (get params "count" "0"))
        body (a/chan)]
    ;; create a goroutine that emits incrementing numbers once every 100 milliseconds
    (a/go-loop [i 0]
      (if (< i cnt)
        (let [_ (a/<! (a/timeout 100))]
          (a/>! body (str i "\n"))
          (recur (inc i)))
        (a/close! body)))
    ;; return a response containing the coerced channel as the body
    {:status  200
     :headers {"content-type" "text/plain"}
     :body    (s/->source body)}))

(defroutes app-routes
  (GET "/hello" [] hello-world-handler)
  (GET "/delayed_hello" [] delayed-hello-world-handler)
  (GET "/numbers" [] streaming-numbers-handler)
  (route/not-found "No such page."))

(def app
  (-> app-routes
      wrap-params))
