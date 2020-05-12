(defproject wheninhistory "0.1.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [secretary "1.2.3"]
                 [compojure "1.6.1"]
                 [yogthos/config "1.1.7"]
                 [ring "1.7.1"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [soda-ash "0.83.0"]
                 [markdown-to-hiccup "0.6.2"]
                 [clj-fuzzy "0.4.1"]
                 [org.clojure/core.async "1.1.587"]
                 [coderafting/humane-time "0.1.0"]
                 [coderafting/memsearch "0.1.0"]
                 [cljs-http "0.1.46"]]
  
  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]
  
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  
  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler wheninhistory.handler/dev-handler}
  
  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   [day8.re-frame/tracing "0.5.1"]]

    :plugins      [[lein-figwheel "0.5.16"]]}
   :prod {:dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]}
   :uberjar {:source-paths ["env/prod/clj"]
             :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]
             :omit-source  true
             :main         wheninhistory.server
             :aot          [wheninhistory.server]
             :uberjar-name "wheninhistory.jar"
             :prep-tasks   ["compile" ["cljsbuild" "once" "min"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "wheninhistory.core/mount-root"}
     :compiler     {:main                 wheninhistory.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           day8.re-frame-10x.preload]
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true
                                           "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                    :external-config      {:devtools/config {:features-to-install :all}}}}


    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            wheninhistory.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}]})
