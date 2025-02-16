# Secure base image
FROM eclipse-temurin:17-jdk-alpine

# Set non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy app
WORKDIR /app
COPY --chown=appuser:appgroup target/authentication-service.jar app.jar

# Expose port
EXPOSE 8080

# Run with limited permissions
ENTRYPOINT ["java", "-jar", "app.jar"]
