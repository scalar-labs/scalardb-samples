FROM docker.io/busybox:1.32 AS tools
FROM openjdk:8u292-jre-slim

WORKDIR /

ADD order-service.tar .

WORKDIR /order-service

COPY order-service.properties database.properties

ENTRYPOINT ["./bin/sample-order-service", "--config", "database.properties"]

EXPOSE 10020
