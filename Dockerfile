# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml ./
COPY app/provider-facade/pom.xml app/provider-facade/pom.xml
COPY app/provider-shard/pom.xml app/provider-shard/pom.xml
COPY app/common/provider-dal/pom.xml app/common/provider-dal/pom.xml
COPY app/common/provider-manager/pom.xml app/common/provider-manager/pom.xml
COPY app/biz/provider-service-impl/pom.xml app/biz/provider-service-impl/pom.xml
COPY app/provider-web/pom.xml app/provider-web/pom.xml
COPY deps/kip-open-common-1.0-SNAPSHOT.jar /tmp/deps/kip-open-common-1.0-SNAPSHOT.jar
COPY deps/kip-open-common-1.0-SNAPSHOT.pom /tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B install:install-file \
    -Dfile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.jar \
    -DpomFile=/tmp/deps/kip-open-common-1.0-SNAPSHOT.pom

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/provider-web -am dependency:go-offline

COPY app ./app

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl app/provider-web -am -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/app/provider-web/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
