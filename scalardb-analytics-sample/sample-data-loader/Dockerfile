FROM eclipse-temurin:17-jdk-jammy AS builder

COPY . /app
WORKDIR /app

RUN chmod +x gradlew && ./gradlew installDist

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/build/install/sample-data-loader /app
