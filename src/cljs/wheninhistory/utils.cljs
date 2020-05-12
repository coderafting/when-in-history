(ns wheninhistory.utils
  (:require [cljs-time.core :as t]
            [cljs.reader :as r]
            [humane-time.core :as ht]))

(def month-index
  {"1" "Jan" "2" "Feb" "3" "Mar" "4" "Apr" "5" "May" "6" "June"
   "7" "Jul" "8" "Aug" "9" "Sep" "10" "Oct" "11" "Nov" "12" "Dec"})

(defn date-format
  [ddmmyyyy-string]
  (let [ds (clojure.string/split ddmmyyyy-string #"-")
        d (first ds)
        m (month-index (second ds))
        y (last ds)]
    {:day (r/read-string d)
     :month (r/read-string (second ds))
     :year (r/read-string y)
     :date (str m " " d ", " y)
     :datetime (t/date-time (r/read-string y) (r/read-string (second ds)) (r/read-string d))}))

(defn human-readable-timeline
  [event]
  (let [start (:start event)
        end (:end event)
        time-formats (:time-formats event)
        valid-start-time-format? (:start-format time-formats)
        valid-end-time-format? (:end-format time-formats)]
    (if end
      (cond
        (and valid-start-time-format? valid-end-time-format?) (str (ht/readable-period {:start start :end end}) ".")
        valid-start-time-format? (str (ht/readable-moment start {:prefix "Started"}) " |" " Ended about " end " ago" ".")
        valid-end-time-format? (str "Started about " start " ago" " | " (ht/readable-moment end {:prefix "Ended"}) ".")
        :else (str "Started about " start " ago" " |" " Ended about " end " ago" "."))
      (cond
        valid-start-time-format? (str (ht/readable-moment start) ".")
        :else (str "Happened about " start " ago" ".")))))
