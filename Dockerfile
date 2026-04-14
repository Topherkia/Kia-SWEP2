# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/target/Kia-SWEP2-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=builder /app/target/dependency /app/lib
COPY db/schema.sql /app/db/schema.sql

RUN useradd -m appuser && chown -R appuser:appuser /app
USER appuser

ENTRYPOINT ["java", "-cp", "/app/app.jar:/app/lib/*", "w1.ShoppingCartCalculator"]
