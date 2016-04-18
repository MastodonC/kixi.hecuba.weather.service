(ns kixi.hecuba.weather.service
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clj-kafka.producer :as kafka])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Path to config edn file."
    :default "config/settings.edn"]
   ["-u" "--username USERNAME" "Your username."]
   ["-p" "--password PASSWORD" "Your password."]])

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn gen-message [entityid entityaction])

(defn send-to-kafka [args-map entities]
  (let [zk (kafka/brokers {"zookeeper.connect" (:zookeeper args-map)})
        kafka-producer (kafka/producer {"metadata.broker.list" (:kafka-brokerlist args-map)
                                        "serializer.class" "kafka.serializer.DefaultEncoder"
                                        "partitioner.class" "kafka.producer.DefaultPartitioner"})]
    (map (fn [entity]
           (kafka/send-message producer
                               (message (.getBytes (gen-message entity (:entity-action args-map)))))) entities)))

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
    (if (= true (:debugmode args-map))
      (println "In debug mode, not sending to Kafka")
      (send-to-kafka args-map entities)
      )))

(defn -main [& args]
  (let [{:keys [config username password] :as opts} (:options (parse-opts args cli-options))
        {:keys [project-id api-endpoint entity-type entity-action max-entries-per-page zookeeper kafka-brokerlist debugmode]} (load-config config)
        args-map {:username username
                  :password password
                  :api-endpoint api-endpoint
                  :entity-type entity-type
                  :entity-action entity-action
                  :max-entries-per-page max-entries-per-page
                  :zookeeper zookeeper
                  :kafka-brokerlist kafka-brokerlist
                  :debugmode debugmode}]
    (get-entity-ids args-map)))
