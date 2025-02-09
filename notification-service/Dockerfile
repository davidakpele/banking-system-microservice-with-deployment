# Dockerfile for Notification-service
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/notification-service.jar app.jar
EXPOSE 8070
ENTRYPOINT ["java", "-jar", "app.jar"]