# Dockerfile for history-service
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/history-service.jar app.jar
EXPOSE 8050
ENTRYPOINT ["java", "-jar", "app.jar"]