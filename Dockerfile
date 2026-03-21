# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY .mvn ./.mvn
COPY pom.xml ./
COPY app/common/auth-common/pom.xml app/common/auth-common/pom.xml
COPY app/common/auth-manager/pom.xml app/common/auth-manager/pom.xml
COPY app/common/auth-dal/pom.xml app/common/auth-dal/pom.xml
COPY app/biz/auth-service-impl/pom.xml app/biz/auth-service-impl/pom.xml
COPY app/auth-web/pom.xml app/auth-web/pom.xml
COPY deps/kip-open-common-1.0-SNAPSHOT.jar /tmp/deps/kip-open-common-1.0-SNAPSHOT.jar
COPY deps/kip-open-common-1.0-SNAPSHOT.pom /tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    rm -f /root/.m2/repository/xyz/kip/kip-open-common/maven-metadata-local.xml && \
    rm -rf /root/.m2/repository/xyz/kip/kip-open-common/1.0-SNAPSHOT && \
    mvn -B install:install-file \
    -Dfile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.jar \
    -DpomFile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/auth-web -am dependency:go-offline

COPY app ./app

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/auth-web -am -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/app/auth-web/target/auth-web-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 5001
EXPOSE 8720
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
