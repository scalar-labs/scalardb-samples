FROM eclipse-temurin:8-jre-jammy

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

COPY customer-service.properties database.properties

ENTRYPOINT ["./bin/customer-service", "--config", "database.properties"]

EXPOSE 10010
