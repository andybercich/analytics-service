FROM gradle:8-jdk17 AS build

WORKDIR /workspace

COPY common-events /workspace/common-events
WORKDIR /workspace/common-events
RUN chmod +x gradlew && ./gradlew publishToMavenLocal --no-daemon

COPY analytics-service /workspace/analytics-service
WORKDIR /workspace/analytics-service
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre

RUN apt-get update \
 && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --create-home appuser \
 && mkdir -p /data/reports /app/logs \
 && chown -R appuser /data/reports /app/logs

WORKDIR /app

COPY --from=build /workspace/analytics-service/build/libs/app.jar app.jar

USER appuser

ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true"

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]