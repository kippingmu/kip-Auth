# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml ./
COPY app ./app
COPY deps/kip-open-common-root/maven-metadata-local.xml /tmp/deps/root-metadata.xml
COPY deps/kip-open-common/ /tmp/deps/kip-open-common/

RUN --mount=type=cache,target=/root/.m2 \
    rm -rf /root/.m2/repository/xyz/kip/kip-open-common && \
    mkdir -p /root/.m2/repository/xyz/kip/kip-open-common/1.0-SNAPSHOT && \
    cp /tmp/deps/root-metadata.xml /root/.m2/repository/xyz/kip/kip-open-common/maven-metadata-local.xml && \
    cp /tmp/deps/kip-open-common/* /root/.m2/repository/xyz/kip/kip-open-common/1.0-SNAPSHOT/ && \
    mvn -B -ntp -pl app/auth-web -am -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/app/auth-web/target/auth-web-1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 5001
EXPOSE 8720
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
