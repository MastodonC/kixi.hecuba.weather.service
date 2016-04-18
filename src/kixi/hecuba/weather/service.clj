(ns kixi.hecuba.weather.service
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clj-kafka.producer :as kafka]
            [clj-kafka.zk :as zk])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Path to config edn file."
    :default "config/settings.edn"]
   ["-u" "--username USERNAME" "Your username."]
   ["-p" "--password PASSWORD" "Your password."]])

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn gen-message [entity-id entity-type entity-action]
  (json/write-str {:entity-id entity-id
                   :entity-type entity-type
                   :entity-action entity-action}))

(defn send-to-kafka [args-map entity]
  (let [{:keys [kafka-producer entity-type entity-action kafka-topic]} args-map]
    (kafka/send-message kafka-producer
                        (kafka/message kafka-topic
                                       (.getBytes (gen-message
                                                   (get entity "entity_id")
                                                   entity-type
                                                   entity-action))))))

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
  (-> (run-api-search args-map)
      (get-in ["entities"])))

(defn -main [& args]
  (let [{:keys [config username password] :as opts} (:options (parse-opts args cli-options))
        base-system  (load-config config)
        system (assoc base-system :kafka-producer (kafka/producer {"metadata.broker.list" (:kafka-brokerlist base-system)
                                                                   "serializer.class" "kafka.serializer.DefaultEncoder"
                                                                   "partitioner.class" "kafka.producer.DefaultPartitioner"}))]
    (->> (get-entity-ids system)
         (run! (partial send-to-kafka system)))))
