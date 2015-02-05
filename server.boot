#!/usr/bin/env boot
 
(set-env! 
  :dependencies 
  #(into % '[[org.clojure/data.json "0.2.5"]
             [ring/ring-core "1.3.2"]
             [ring/ring-jetty-adapter "1.3.2"]]))

(require '[ring.adapter.jetty     :as jetty]
         '[clojure.data.json      :as json]
         '[ring.middleware.params :refer [wrap-params]]
         '[ring.util.response     :refer [file-response response]])

(defn static
  [request]
  "Handle static file delivery"
  (let [uri (:uri request)
        path (str "./public" uri)]
    (if (= uri "/comments.json")
      (file-response "./_comments.json")
      (file-response path))))

(defn save-comments
  [request]
  "Save the comments to the json file, and return the new data"
  (let [data (json/read-str (slurp "./_comments.json"))
        input (:form-params request)
        out (concat data [input])
        new-json (json/write-str out)]
    (spit "./_comments.json" new-json)
    (response new-json)))

(defn handler
  [request]
  "Simple handler that delegates based on the request type"
  (case (:request-method request)
    :post (save-comments request)
    :get (static request)))

(def app
  "Add middleware to the main handler"
  (wrap-params handler))

(defn -main
  [& args]
  (jetty/run-jetty app {:port 3000}))
