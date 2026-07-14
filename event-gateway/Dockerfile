FROM gradle:8.14.3-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle build.gradle ./
COPY event-gateway/build.gradle event-gateway/build.gradle
COPY account-service/build.gradle account-service/build.gradle
COPY event-gateway/src event-gateway/src
RUN gradle :event-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends wget \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home spring \
    && mkdir -p /data && chown -R spring:spring /app /data
COPY --from=build --chown=spring:spring /workspace/event-gateway/build/libs/event-gateway-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
