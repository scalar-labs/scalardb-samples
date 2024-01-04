FROM openjdk:17-slim AS builder

COPY . /app
WORKDIR /app

RUN ./gradlew shadowJar

FROM openjdk:17-slim

COPY --from=builder /app/build/libs/sample-data-loader-all.jar /app.jar
