FROM eclipse-temurin:8-jre-jammy

WORKDIR /

ADD order-service.tar .

WORKDIR /order-service

COPY order-service.properties database.properties

ENTRYPOINT ["./bin/sample-order-service", "--config", "database.properties"]

EXPOSE 10020
