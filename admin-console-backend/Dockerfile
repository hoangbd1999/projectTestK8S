# ============================================
# Stage 1: Build the application
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and POM first (for dependency caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make mvnw executable and download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application JAR (skip tests for CI speed)
RUN ./mvnw package -DskipTests -B

# ============================================
# Stage 2: Create the runtime image
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Add curl for health check probes
RUN apk add --no-cache curl

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy config directory (logback.xml etc.)
COPY config/ config/

# Expose application port
EXPOSE 6001

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh

# Run with dev profile (uses environment variables)
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar", \
  "--spring.profiles.active=dev"]
