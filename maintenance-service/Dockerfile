# Use an official Java runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY target/maintenance-service.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8390

# Run the jar file
ENTRYPOINT ["java","-jar","app.jar"]
