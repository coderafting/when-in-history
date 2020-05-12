(ns wheninhistory.effects
  (:require [re-frame.core :as rf]
            [memsearch.core :as ms]
            [cljs-http.client :as http]
            [wheninhistory.token :as token])
  (:require-macros [wheninhistory.macros :refer [logger-token]]))

(defonce auth-token (or (str "Bearer " (logger-token)) token/creds))

(defn log-api-req-map
  [entity data]
  (http/post "https://product-logger.herokuapp.com/api/v1/demo"
             {:with-credentials? true
              :headers {"Authorization" token/creds}
              :json-params {"project-name" "demo"
                            "action" "create"
                            "entity" entity
                            "data" data}}))

(rf/reg-fx
 ::api-call
 (fn [{:keys [entity data]}]
   (log-api-req-map entity data)))

(rf/reg-fx
 ::api-calls
 (fn [params-coll]
   (doseq [p params-coll]
     (log-api-req-map (:entity p) (:data p)))))

(rf/reg-fx
 ::index-db
 (fn [{:keys [index-init indexing-content]}]
   (reset! index-init (ms/text-index indexing-content {:maintain-actual? true}))))