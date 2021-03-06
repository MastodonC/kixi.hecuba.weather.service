(ns kixi.hecuba.weather.service
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]
            [clj-kafka.producer :as kafka]
            [clj-kafka.zk :as zk])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Path to config edn file."
    :default "config/settings.edn"]
   ["-u" "--username USERNAME" "Your username."]
   ["-p" "--password PASSWORD" "Your password."]
   ["-a" "--aprogramme APROGRAMME" "The Hecuba Programme id."]])

(defn fetch-kafka-broker-endpoints [zk-host]
  (zk/broker-list (zk/brokers {"zookeeper.connect" zk-host})))

(defn load-config [filename]
  (with-open [r (io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn gen-message [programme-id entity-id property-code device_id entity-type entity-action]
  (json/write-str {:programme_id programme-id
                   :entity-id entity-id
                   :property-code property-code
                   :device_id device_id
                   :entity-type entity-type
                   :entity-action entity-action}))

(defn send-to-kafka [args-map entity]
  (let [{:keys [kafka-producer entity-type programme-id entity-action kafka-topic]} args-map]
    (kafka/send-message kafka-producer
                        (kafka/message kafka-topic
                                       (.getBytes (gen-message
                                                   (get entity "programme_id")
                                                   (get entity "entity_id")
                                                   (get entity "property_code")
                                                   (-> (get-in entity ["devices"])
                                                       first
                                                       (get "device_id"))
                                                   entity-type
                                                   entity-action))))))
(defn run-api-search [{:keys [api-endpoint entity-type programme-id  max-entries-per-page username password] :as args-map}]
  (let [url-to-get (str api-endpoint
                        "entities/?q=property_type:\""
                        entity-type
                        "\" AND programme_id:"
                        programme-id
                        "&page=0&size="
                        max-entries-per-page
                        "&sort_key=programme_name.lower_case_sort&sort_order=asc")]
    (try (let [response-json (-> (:body (client/get
                                         url-to-get
                                         {:basic-auth [username
                                                       password ]
                                          :headers {"X-Api-Version" "2"}
                                          :content-type :json
                                          :socket-timeout 20000
                                          :conn-timeout 20000}))
                                 (json/read-str))]
           response-json)
         (catch Exception e (timbre/error e)))))

(defn get-entity-ids [args-map]
  (-> (run-api-search args-map)
      (get-in ["entities"])))

(defn -main [& args]
  (let [{:keys [config username password aprogramme] :as opts} (:options (parse-opts args cli-options))
        base-system  (assoc (load-config config) :username username :password password :programme-id aprogramme)
        system (assoc base-system :kafka-producer (kafka/producer {"metadata.broker.list" (fetch-kafka-broker-endpoints (:zookeeper base-system))
                                                                   "serializer.class" "kafka.serializer.DefaultEncoder"
                                                                   "partitioner.class" "kafka.producer.DefaultPartitioner"}))]
    (->> (get-entity-ids system)
         (run! (partial send-to-kafka system)))))
