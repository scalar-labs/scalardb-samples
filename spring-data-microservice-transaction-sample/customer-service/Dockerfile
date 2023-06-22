FROM openjdk:8

WORKDIR /

ADD customer-service.tar .

WORKDIR /customer-service

ENTRYPOINT ["./bin/customer-service"]

EXPOSE 10010
