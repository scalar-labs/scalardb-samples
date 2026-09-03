FROM eclipse-temurin:8-jre-jammy

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

ENTRYPOINT ["./bin/customer-service"]

EXPOSE 10010
