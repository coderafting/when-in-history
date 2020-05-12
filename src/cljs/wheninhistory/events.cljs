(ns wheninhistory.events
  (:require [re-frame.core :as rf]
            [wheninhistory.db :as db]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [wheninhistory.effects :as effects]))

; used for logging session
(defonce current-session-id (str (random-uuid)))

(def datetime-formatter (tf/formatters :date-hour-minute-second))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-fx
 ::index-and-log-session
 (fn [_ _]
   {::effects/index-db {:index-init db/db-index 
                        :indexing-content db/indexing-content}
    ::effects/api-call {:entity "session" 
                        :data {"id" current-session-id
                               "datetime" (tf/unparse datetime-formatter (t/time-now))}}}))

(rf/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(rf/reg-event-db
 ::set-search-input
 (fn [db [_ search-input]]
   (assoc db :current-search-input search-input)))

(rf/reg-event-db
 ::set-random-result
 (fn [db [_ result]]
   (assoc db :random-result result)))

(rf/reg-event-db
 ::set-random-req
 (fn [db [_ rand-req?]]
   (assoc db :random-req? rand-req?)))

(rf/reg-event-fx
 ::trigger-random-result ; gets into action when a user clicks on the logo
 (fn [_ [_ result]]
   {:dispatch-n (list [::set-random-req true]
                      [::set-random-result result]
                      [::set-search-input ""])}))

(defn update-search-inputs
  "The valid-intrvl is in millisec."
  [inputs-vec current-timestamp valid-intrvl & opts]
  (vec (remove nil?
                 (map #(let [interval (t/interval (:timestamp %) current-timestamp)
                             int-ms (t/in-millis interval)]
                         (if (< int-ms valid-intrvl) 
                           (if (= (:remove (first opts)) :old) %)
                           (if (= (:remove (first opts)) :recent) %)))
                      inputs-vec))))

(rf/reg-event-db
 ::append-search-inputs
 (fn [db [_ new-input intervl]]
   (let [current-inputs (:search-inputs db)]
     (if (or (nil? current-inputs) (empty? current-inputs))
       (assoc db :search-inputs [new-input])
       (assoc db :search-inputs (conj (update-search-inputs current-inputs (:timestamp new-input) intervl {:remove :recent}) 
                                      new-input))))))

(defn event-log-data
  [input]
  {:entity "event"
   :data {"id" (str (random-uuid))
          "name" "search-text"
          "datetime" (tf/unparse datetime-formatter (t/time-now))
          "session-id" current-session-id
          "text" (:text input)}})

(defn event-api-req-maps
  [search-inputs]
  (mapv event-log-data search-inputs))

(rf/reg-event-db
 ::update-non-logged
 (fn [db [_ non-logged-inputs]]
   (assoc db :search-inputs non-logged-inputs)))

(rf/reg-event-fx
  ::log-search-inputs
  (fn [_ [_ inputs]]
    {::effects/api-calls (event-api-req-maps inputs)}))

(rf/reg-event-fx
  ::log-and-update-search-inputs
  (fn [coeffects [_ intrvl]]
    (let [db (:db coeffects)
          inputs (:search-inputs db)
          non-logged-inputs (update-search-inputs inputs (t/time-now) intrvl {:remove :old})
          log-data (update-search-inputs inputs (t/time-now) intrvl {:remove :recent})]
      {:dispatch-n (list [::log-search-inputs log-data] 
                         [::update-non-logged non-logged-inputs])})))

(rf/reg-event-fx
 ::trigger-search-and-log
 (fn [_ [_ rand-req-status search-input]]
   {:dispatch-later [(when (true? rand-req-status) {:ms 100 :dispatch [::set-random-req false]})
                     (when (> (count (:text search-input)) 2)
                       {:ms 250 :dispatch [::set-search-input search-input]})
                     (when (> (count (:text search-input)) 2) 
                       {:ms 250 :dispatch [::append-search-inputs search-input 3000]})
                     {:ms 3000 :dispatch [::log-and-update-search-inputs 3000]}]}))
