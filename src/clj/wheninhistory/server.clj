(ns wheninhistory.server
  (:require [wheninhistory.handler :refer [handler]]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (or (if (env :port) (Integer/parseInt (str (env :port)))) 3000)]
     (run-jetty handler {:port port :join? false})))
