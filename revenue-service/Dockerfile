# Dockerfile for Revenue-service
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/revenue-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]