# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM gradle:8.14.3-jdk21 AS build
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
