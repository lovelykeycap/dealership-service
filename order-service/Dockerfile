FROM gradle:8.12-jdk21 AS builder
WORKDIR /workspace

COPY . .
RUN gradle --no-daemon :order-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/order-service/build/libs/*.jar /app/order-service.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/order-service.jar"]
