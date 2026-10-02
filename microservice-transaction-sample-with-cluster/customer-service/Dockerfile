FROM eclipse-temurin:8-jre-jammy

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

# The configuration file is deliberately not copied in. It is mounted at
# /customer-service/database.properties by docker-compose, and the config path is passed by the
# compose `command`, so that the same image runs against both deployment patterns.
ENTRYPOINT ["./bin/customer-service"]

EXPOSE 10010
