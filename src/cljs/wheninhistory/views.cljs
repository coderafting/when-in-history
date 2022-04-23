(ns wheninhistory.views
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [wheninhistory.subs :as subs]
            [wheninhistory.events :as events]
            [wheninhistory.db :as db]
            [wheninhistory.utils :as u]
            [wheninhistory.search :as s]
            [soda-ash.core :as sa]
            [cljs-time.core :as t]))

(defn logo-center 
  []
  [sa/Container {:textAlign "center"}
   [sa/Image {:src "img/logo.png"
              :size "medium"
              :centered true
              :onClick #(rf/dispatch [::events/trigger-random-result 
                                      (get db/db-vec (rand-int (count db/events)))])}]])

(defn search-placeholder []
  (get-in (get db/db-vec (rand-int (count db/events))) [:event :name]))

(defn search-area 
  []
  (let [rand-req? (rf/subscribe [::subs/random-req])
        input (reagent/atom "")]
    (fn []
      [sa/Search {:input {:fluid true}
                  :fluid true
                  :minCharacters 2
                  :value (if @rand-req? "" @input)
                  :placeholder (str "Try: " (or (search-placeholder) "world war"))
                  :showNoResults false
                  :onSearchChange #(do (reset! input (-> % .-target .-value))
                                       (rf/dispatch [::events/trigger-search-and-log 
                                                     @rand-req? 
                                                     {:text (-> % .-target .-value) :timestamp (t/time-now)}]))}])))

(defn no-results-view [input]
  [:div {:align "center"}
   [:p#no-result (str "We don't have this information yet " "🐶")]
   [sa/Button {:content "Show results from Google!"
               :color "blue"
               ;:inverted true
               :onClick #(.open js/window (s/google-search-string input))}]])

(defn result-card
  [res]
  (if res 
    (let [eve (:event res)]
      [:div
       [:div
        [sa/ListSA {:size "large"}
         [sa/ListItem
          [sa/ListContent
           [sa/ListHeader {:content (:name eve)
                           :as "a"
                           :onClick #(.open js/window (:source eve))}]]]]]
       [:div#dates
        [:p 
         (str "🕑 "
              (if (get-in eve [:time-formats :start-format])
                (:date (u/date-format (:start eve)))
                (str "About " (:start eve) " ago"))
              " ")
         (if (:end eve)
           (if (get-in eve [:time-formats :end-format])
             [:b {:dangerouslySetInnerHTML {:__html "&ndash;"}}]
             [:b {:dangerouslySetInnerHTML {:__html "&ndash;"}}]))
         (if (:end eve)
           (if (get-in eve [:time-formats :end-format])
             (str " " (:date (u/date-format (:end eve))))
             (str " about " (:end eve) " ago")))]]
       [:p#human-dates (u/human-readable-timeline eve)]
       [sa/Divider]])))

(defn results-view 
  "TODO: random-uuid is not good as keys here, change it. Take one of the unique attributes of the event"
  [res]
  [:div
   (for [r res]
     ^{:key (random-uuid)} ; see TODO
     [result-card r])])

(defn results []
  (let [res (rf/subscribe [::subs/search-results])
        rand-res (rf/subscribe [::subs/random-result])
        rand-req (rf/subscribe [::subs/random-req])]
    (cond 
      (not (empty? (:result @res))) [results-view (:result @res)]
      (true? @rand-req) [result-card @rand-res]
      (empty? (:input @res)) [:div]
      :else [no-results-view (:input @res)])))

;; About and other details
(defn about-details
  []
  [:div
   [sa/Grid {:stackable true}
    [sa/GridRow
     [sa/GridColumn {:textAlign "center"}
      [:div
       [sa/Icon {:name "user secret"
                 :size "large"
                 :color "black"
                 :link true
                 :onClick #(.open js/window "https://coderafting.com/#/about")}]
       [sa/Icon]
       [sa/Icon {:name "github"
                 :size "large"
                 :color "black"
                 :link true
                 :onClick #(.open js/window "https://github.com/coderafting/when-in-history")}]]]]]])

;; Footer

(defn current-year []
  (t/year (t/now)))

(defn year-range []
  (if (= 2020 (current-year))
    "2020"
    (str "2020-" (current-year))))

(defn copyright []
  (str "Copyright © "
       (year-range)
       " WhenInHistory. All rights reserved."))

(defn footer []
  [:div
   [sa/Grid {:stackable true}
    [sa/GridColumn {:textAlign "center"}
     [:p#footer (copyright)]]]])


;; home

(defn home-panel []
  [:div 
   [:div#logo-section [logo-center]]
   [:div#search-section [search-area]]
   [:div#result-section [results]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [:div
     [show-panel @active-panel]
     [about-details]
     [footer]]))
