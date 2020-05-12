(ns wheninhistory.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [wheninhistory.events :as events]
            [wheninhistory.effects]
            [wheninhistory.subs]
            [wheninhistory.routes :as routes]
            [wheninhistory.views :as views]
            [wheninhistory.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (rf/dispatch [::events/index-and-log-session])
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
