FROM docker.io/busybox:1.32 AS tools
FROM openjdk:8u292-jre-slim

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

COPY customer-service.properties database.properties

ENTRYPOINT ["./bin/customer-service", "--config", "database.properties"]

EXPOSE 10010
