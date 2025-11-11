# ============================================================================
# Multi-Stage Dockerfile for Weather API (Production-Optimized)
# ============================================================================
# Leverages Phase 5 layered JARs for 10x faster Docker rebuilds, since all the dependencies are cached, it can be fetched from there way faster
# ============================================================================

# ============================================================================
# Stage 1: Dependency Resolution (Changes Rarely → Cached)
# ============================================================================
FROM gradle:8.5-jdk21-alpine AS dependencies

WORKDIR /workspace/app

# Copy only dependency-related files
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle gradle

# Download dependencies (this layer is cached unless dependencies change)
RUN gradle dependencies --no-daemon || true

# ============================================================================
# Stage 2: Build Application (Changes Frequently)
# ============================================================================
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /workspace/app

# Copy cached dependencies from previous stage
COPY --from=dependencies /home/gradle/.gradle /home/gradle/.gradle
COPY --from=dependencies /workspace/app /workspace/app

# Copy source code
COPY src src

# Build application with layered JAR
RUN gradle bootJar --no-daemon -x test && \
    java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# ============================================================================
# Stage 3: Runtime (Minimal Production Image)
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

# Metadata
LABEL maintainer="yashaswi"
LABEL application="weatherapi"
LABEL version="1.0.0"

# Security: Create non-root user
RUN addgroup -g 1000 weatherapi && \
    adduser -D -u 1000 -G weatherapi weatherapi

WORKDIR /app

# Copy layered JAR (each layer is cached independently)
COPY --from=builder --chown=weatherapi:weatherapi /workspace/app/extracted/dependencies/ ./
COPY --from=builder --chown=weatherapi:weatherapi /workspace/app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=weatherapi:weatherapi /workspace/app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=weatherapi:weatherapi /workspace/app/extracted/application/ ./

# Switch to non-root user
USER weatherapi

# Expose port
EXPOSE 8080

# Health check (self-healing)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options optimized for containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

# Start application using Spring Boot Loader
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
