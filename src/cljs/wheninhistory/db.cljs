(ns wheninhistory.db)

(def ; Sample DB
  ^{:doc "This is a temporary db. Note that :start cannot be nil."
    :todo "fetch events from wiki APIs and create a db on the server side."}
  events
  {1 {:event {:name "Launch of WhenInHistory" :start "12-5-2020" :end nil :time-formats {:start-format true :end-format false} :source "https://wheninhistory.com"}}
   2 {:event {:name "World War 1" :start "28-7-1914" :end "11-11-1918" :time-formats {:start-format true :end-format true} :source "https://en.wikipedia.org/wiki/World_War_I"}}
   3 {:event {:name "World War 2" :start "1-9-1939" :end "2-9-1945" :time-formats {:start-format true :end-format true} :source "https://en.wikipedia.org/wiki/World_War_II"}}
   4 {:event {:name "Independence of India" :start "15-8-1947" :end nil :time-formats {:start-format true :end-format false} :source "https://en.wikipedia.org/wiki/Independence_Day_(India)"}}
   5 {:event {:name "Independence of United States of America, USA" :start "4-7-1776" :end nil :time-formats {:start-format true :end-format false} :source "https://en.wikipedia.org/wiki/Independence_Day_(United_States)"}}
   6 {:event {:name "Fall of the Berlin Wall" :start "9-11-1989" :end nil :time-formats {:start-format true :end-format false} :source "https://en.wikipedia.org/wiki/Fall_of_the_Berlin_Wall"}}
   7 {:event {:name "Reunification of Germany" :start "3-10-1990" :end nil :time-formats {:start-format true :end-format false} :source "https://en.wikipedia.org/wiki/German_Unity_Day"}}
   8 {:event {:name "Creation of Universe" :start "13.8 billion years" :end nil :time-formats {:start-format false :end-format false} :source "https://en.wikipedia.org/wiki/Universe"}}
   9 {:event {:name "Creation of Earth" :start "4.54 billion years" :end nil :time-formats {:start-format false :end-format false} :source "https://en.wikipedia.org/wiki/History_of_Earth"}}
   10 {:event {:name "Creation of Euro" :start "1-1-1999" :end nil :time-formats {:start-format true :end-format false} :source "https://en.wikipedia.org/wiki/History_of_the_euro"}}})

(def db-vec (vec (vals events)))

(def indexing-content
  (map #(hash-map :id (first %) :content (get-in (last %) [:event :name])) 
       events))

(def db-index (atom {}))

(def search-results 
  {:input ""
   :result []})

(def default-db
  {:name "WhenInHistory"
   :events events
   :search-results search-results})
