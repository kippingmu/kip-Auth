# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml ./
COPY app ./app
COPY deps/kip-open-common-1.0-SNAPSHOT.jar /tmp/deps/kip-open-common-1.0-SNAPSHOT.jar
COPY deps/kip-open-common-1.0-SNAPSHOT.pom /tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    rm -f /root/.m2/repository/xyz/kip/kip-open-common/maven-metadata-local.xml && \
    rm -rf /root/.m2/repository/xyz/kip/kip-open-common/1.0-SNAPSHOT && \
    mvn -B -ntp install:install-file \
    -Dfile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.jar \
    -DpomFile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -pl app/auth-web -am -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/app/auth-web/target/auth-web-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 5001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
