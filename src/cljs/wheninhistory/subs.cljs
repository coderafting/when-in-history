(ns wheninhistory.subs
  (:require [re-frame.core :as rf]
            [wheninhistory.search :as s]))

(rf/reg-sub
 ::name
 (fn [db _]
   (:name db)))

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 ::events
 (fn [db _]
   (:events db)))

(rf/reg-sub
 ::search-inputs
 (fn [db _]
   (:search-inputs db)))

(rf/reg-sub
 ::current-search-input
 (fn [db _]
   (:current-search-input db)))

(rf/reg-sub
 ::search-results
 (fn [_ _]
   (rf/subscribe [::current-search-input]))
 (fn [in _]
   (s/search (:text in))))

(rf/reg-sub
 ::random-result
 (fn [db _]
   (:random-result db)))

(rf/reg-sub
 ::random-req
 (fn [db _]
   (:random-req? db)))

(rf/reg-sub
 ::log-resp
 (fn [db _]
   (:log-resp db)))
