(ns kixi.hecuba.weather.service
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Path to config edn file."
    :default "config/settings.edn"]])

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn find-entities [project-id api-endpoint entity-type]
  (println project-id)
  (println api-endpoint)
  (println entity-type))

(defn -main [& args]
  (let [config (-> (parse-opts args cli-options) :options :config)
        {:keys [project-id api-endpoint entity-type]} (load-config config)]
    (find-entities project-id
                   api-endpoint
                   entity-type)))
