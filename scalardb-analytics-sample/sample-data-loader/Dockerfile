# You can see sha256 hash in the following page:
# - https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:17.0.21_9-jdk-resolute@sha256:c19b0e2ecdec69220c6c69e7fc7f4ab5b233ccfcce65f0b87c4411e71ca82fdc AS builder

COPY . /app
WORKDIR /app

RUN chmod +x gradlew && ./gradlew installDist

FROM eclipse-temurin:17.0.21_9-jre-resolute@sha256:2e6cde8b9d2138aa6393216ca79567095d0a63b1907f898a04cb4c2ddfb60e11

RUN export DEBIAN_FRONTEND=noninteractive \
 && apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/install/sample-data-loader /app
