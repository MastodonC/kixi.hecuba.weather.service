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

(defn run-api-search [username password project-id api-endpoint entity-type max-entries-per-page]
  (let [url-to-get (str api-endpoint
                        "entities/?q=property_type:"
                        entity-type "&page=0&size="
                        max-entries-per-page
                        "&sort_key=programme_name.lower_case_sort&sort_order=asc")]
    (try (let [response-json (-> (:body (client/get
                                         url-to-get
                                         {:basic-auth [username password]
                                          :headers {"X-Api-Version" "2"}
                                          :content-type :json
                                          :socket-timeout 20000
                                          :conn-timeout 20000}))
                                 (json/read-str))]
           response-json)
         (catch Exception e (println e)))))

(defn get-entity-ids [username password project-id api-endpoint entity-type  max-entries-per-page]
  (let [entities (-> (run-api-search username password project-id api-endpoint entity-type max-entries-per-page)
                     (get-in ["entities"]))]
    (mapv (fn [entity] (get entity "entity_id")) entities)))

(defn -main [& args]
  (let [{:keys [config username password] :as opts} (:options (parse-opts args cli-options))
        {:keys [project-id api-endpoint entity-type max-entries-per-page]} (load-config config)]
    (get-entity-ids
     username
     password
     project-id
     api-endpoint
     entity-type
     max-entries-per-page)))
