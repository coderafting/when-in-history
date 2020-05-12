(ns wheninhistory.search
  (:require [wheninhistory.db :as db]
            [memsearch.core :as ms]))

(defn search
  [input-str]
  {:input input-str
   :result (vals (ms/text-search input-str @db/db-index {:db db/events :sorted? true}))})

(defn google-search-string
  "Sample: https://www.google.com/search?client=firefox-b-d&q=world+war+1"
  [input]
  (str "https://www.google.com/search?client=firefox-b-d&q="
       (clojure.string/join "+" (clojure.string/split input #" "))))
