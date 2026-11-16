FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

RUN --mount=type=secret,id=gradle_properties,target=/root/.gradle/gradle.properties \
    --mount=type=cache,target=/root/.gradle/caches \
    ./gradlew dependencies --no-daemon

COPY src src
COPY newrelic newrelic

RUN --mount=type=secret,id=gradle_properties,target=/root/.gradle/gradle.properties \
    --mount=type=cache,target=/root/.gradle/caches \
    ./gradlew bootJar copyNewRelic --no-daemon -x test


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/build/newrelic/newrelic.jar /app/newrelic.jar
COPY --from=builder /app/build/newrelic/newrelic.yml /app/newrelic.yml

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-jar", "app.jar"]
