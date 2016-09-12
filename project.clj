(defproject kixi.hecuba.weather.service "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
		 [org.clojure/tools.cli "0.3.3"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/data.csv "0.1.3"]
                 [com.taoensso/timbre "4.3.1"]
                 [clj-http "2.1.0"]
                 [clj-kafka "0.3.4"]]

  :main ^:skip-aot kixi.hecuba.weather.service
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
		       :uberjar-name "weather-service.jar"}})
