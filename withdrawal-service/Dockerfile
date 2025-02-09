# Dockerfile for Withdrawal-service
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/withdrawal-service.jar app.jar
EXPOSE 8030
ENTRYPOINT ["java", "-jar", "app.jar"]