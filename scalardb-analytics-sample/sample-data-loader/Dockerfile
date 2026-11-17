# You can see sha256 hash in the following page:
# - https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:17.0.21_9-jdk-resolute@sha256:b5e475b49399b918176adc274c95e02db1057f8d6dbc891dad17d29d811c5a5c AS builder

COPY . /app
WORKDIR /app

RUN chmod +x gradlew && ./gradlew installDist

FROM eclipse-temurin:17.0.21_9-jre-resolute@sha256:d532593b4d17f9455bcc74b4b45046fd0f9799db3b50b3486091e3e33ad99a26

RUN export DEBIAN_FRONTEND=noninteractive \
 && apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/install/sample-data-loader /app
