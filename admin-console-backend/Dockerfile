# ============================================
# Slim Runtime Image (Pre-compiled JAR)
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Add curl for health check probes
RUN apk add --no-cache curl

WORKDIR /app

# Copy the pre-compiled JAR (built locally or by CI)
COPY target/*.jar app.jar

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
