 (defproject canoedaysout "0.3.4"
  :description "The Clojure/Clojurescript webapp powering CanoeDaysOut"
  :url "http://new.canoedaysout.com"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [prismatic/dommy "0.1.2"]
                 [ring "1.2.1"]
                 [compojure "1.1.6"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [clj-refresh-cache "0.1.0-SNAPSHOT"]
  ;;               [yesql "0.4.0"]
    ;;             [org.clojure/java.jdbc "0.3.3"]
     ;;            [mysql/mysql-connector-java "5.1.25"]
                 [congomongo "0.4.1"]]

  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-ring "0.8.10"]]
  :ring {:handler canoedaysout.core/app
  	 :war-exclusions [#"clojurescript.*"
#"closure-compiler.*"
#"google-closure-lib.*"
#"guava.*"
#"rhino.*"
#"jetty.*"
]}

  :source-paths ["src/clj"]
  :profiles {:uberjar {:aot :all}}
  :cljsbuild {
    :builds [{:id "trip"
              :source-paths ["src/cljs/canoedaysout/trip"]
              :compiler {
                         :output-to "resources/public/trip.js"
                         :output-dir "resources/public/out/trip"
                         :optimizations :simple
                         }}

             {:id "input"
              :source-paths ["src/cljs/canoedaysout/input"]
              :compiler {
                         :output-to "resources/public/input.js"
                         :output-dir "resources/public/out/input"
                         :optimizations :simple
                         }}
             ]})
