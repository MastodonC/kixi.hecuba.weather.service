(ns kixi.hecuba.weather.service
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Path to config edn file."
    :default "config/settings.edn"]
   ["-u" "--username USERNAME" "Your username."]
   ["-p" "--password PASSWORD" "Your password."]])

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn run-api-search [args-map]
  (let [url-to-get (str (:api-endpoint args-map)
                        "entities/?q=property_type:"
                        (:entity-type args-map)
                        "&page=0&size="
                        (:max-entries-per-page args-map)
                        "&sort_key=programme_name.lower_case_sort&sort_order=asc")]
    (try (let [response-json (-> (:body (client/get
                                         url-to-get
                                         {:basic-auth [(:username args-map)
                                                       (:password args-map)]
                                          :headers {"X-Api-Version" "2"}
                                          :content-type :json
                                          :socket-timeout 20000
                                          :conn-timeout 20000}))
                                 (json/read-str))]
           response-json)
         (catch Exception e (println e)))))

(defn get-entity-ids [args-map]
  (let [entities (-> (run-api-search args-map)
                     (get-in ["entities"]))]
    (mapv (fn [entity] (get entity "entity_id")) entities)))

(defn -main [& args]
  (let [{:keys [config username password] :as opts} (:options (parse-opts args cli-options))
        {:keys [project-id api-endpoint entity-type max-entries-per-page]} (load-config config)
        args-map {:username username
                  :password password
                  :api-endpoint api-endpoint
                  :entity-type entity-type
                  :max-entries-per-page max-entries-per-page}]
    (get-entity-ids args-map)))
