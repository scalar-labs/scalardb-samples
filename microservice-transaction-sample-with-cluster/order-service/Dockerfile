FROM eclipse-temurin:8-jre-jammy

WORKDIR /

ADD order-service.tar .

WORKDIR /order-service

# The configuration file is deliberately not copied in. It is mounted at
# /order-service/database.properties by docker-compose, and the config path is passed by the
# compose `command`, so that the same image runs against both deployment patterns.
ENTRYPOINT ["./bin/order-service"]

EXPOSE 10020
