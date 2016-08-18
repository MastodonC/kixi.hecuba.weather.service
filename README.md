# kixi.hecuba.weather.service

The Hecuba Weather microservice queries a Hecuba instance via the search API and publishes the entity, programme, device and sensor id's for a specific entity type. This information is converted to JSON and published to a Kafka topic.

## Installation

Clone the repo and then use leiningen to build.

```
lein clean
lein uberjar
```

A template configuration file is supplied in the `config` directory. Create a copy of this file and keep it somewhere safe. Amend the values to reflect your Hecuba and Kafka setup.

## Usage

To run the service use the command line to run the uberjar.

    $ java -jar kixi.hecuba.weather.service-0.1.0-standalone.jar -c/path/to/your/config.edn -umyhecubauser@email -pyourpassword
 

## License

Copyright © 2016 Mastodon C 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
