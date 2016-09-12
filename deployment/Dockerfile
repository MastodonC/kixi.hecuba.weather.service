FROM java:8-jre-alpine
MAINTAINER Jason Bell <jason@mastodonc.com>

ADD https://github.com/just-containers/s6-overlay/releases/download/v1.11.0.1/s6-overlay-amd64.tar.gz /tmp/

RUN tar xzf /tmp/s6-overlay-amd64.tar.gz -C /
RUN apk add --update libgcc libstdc++ bash bash-doc bash-completion

ADD scripts/run_service.sh /opt/run_service.sh
ADD target/uberjar/weather-service.jar /opt/weather-service.jar
ADD config/settings.edn /opt/config.edn

ENTRYPOINT ["/init"]

