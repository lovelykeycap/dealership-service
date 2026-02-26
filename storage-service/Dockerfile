FROM gradle:8.12-jdk21 AS builder
WORKDIR /workspace

COPY . .
RUN gradle --no-daemon :storage-service:bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/storage-service/build/libs/*.jar /app/storage-service.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/storage-service.jar"]
