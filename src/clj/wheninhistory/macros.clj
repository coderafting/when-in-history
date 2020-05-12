(ns wheninhistory.macros)

(defmacro logger-token []
  (System/getenv "token"))