# You can see sha256 hash in the following page:
# - https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:8u512-b09-jre-resolute@sha256:1e292e0e4d11fcb3b0d7561300d8f63607c730cf98e7ab746d91d3ae1585a6c4

RUN export DEBIAN_FRONTEND=noninteractive \
 && apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r --gid 201 scalar \
 && useradd -r --uid 201 -g scalar -s /usr/sbin/nologin scalar

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

COPY customer-service.properties database.properties

USER 201:201

ENTRYPOINT ["./bin/customer-service", "--config", "database.properties"]

EXPOSE 10010
