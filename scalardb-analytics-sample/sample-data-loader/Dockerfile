# You can see sha256 hash in the following page:
# - https://hub.docker.com/_/eclipse-temurin/tags
FROM eclipse-temurin:21.0.13_9-jdk-resolute@sha256:490ee3b757ff32d7a6afdc57120babce54acb2e83a6ecf7d17d87c066cfe3929 AS builder

COPY . /app
WORKDIR /app

RUN chmod +x gradlew && ./gradlew installDist

FROM eclipse-temurin:21.0.13_9-jre-resolute@sha256:556903b1f40c0a0ddc0bd05cf413436a3b2ce48c9ee19fc199434898fd3ea2b9

RUN export DEBIAN_FRONTEND=noninteractive \
 && apt-get update \
 && apt-get upgrade -y --no-install-recommends \
 && apt-get autoremove -y \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/install/sample-data-loader /app
