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
   [ring.middleware.params :refer [wrap-params]]))

(defn hello-world-handler
  [req] {:status  200
         :headers {"content-type" "text/plain"}
         :body    "hello world!"})

(defn delayed-hello-world-handler
  [req]
  (d/timeout!
   (d/deferred)
   1000
   (hello-world-handler req)))

(extend-protocol Renderable
  manifold.deferred.IDeferred
  (render [d _] d))

(defn delayed-hello-world-handler
  [req]
  (s/take!
   (s/->source
    (a/go
      (let [_ (a/<! (a/timeout 1000))]
        (hello-world-handler req))))))

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
