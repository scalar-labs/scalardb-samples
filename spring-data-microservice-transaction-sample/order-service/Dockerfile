# You can see sha256 hash in the following page:
# - https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:21.0.13_9-jre-resolute@sha256:556903b1f40c0a0ddc0bd05cf413436a3b2ce48c9ee19fc199434898fd3ea2b9

RUN export DEBIAN_FRONTEND=noninteractive \
 && apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd -r --gid 201 scalar \
 && useradd -r --uid 201 -g scalar -s /usr/sbin/nologin scalar

WORKDIR /

ADD order-service.tar .

WORKDIR /order-service

USER 201:201

ENTRYPOINT ["./bin/sample-order-service"]

EXPOSE 10020
