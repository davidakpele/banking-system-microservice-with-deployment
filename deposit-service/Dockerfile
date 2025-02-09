# Dockerfile for Deposit-service
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/deposit-service.jar app.jar
EXPOSE 8020
ENTRYPOINT ["java", "-jar", "app.jar"]