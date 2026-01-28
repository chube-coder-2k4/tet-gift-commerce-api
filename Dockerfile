# ========================
# Stage 1: Build Stage
# ========================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

LABEL authors="QuangHuyy"
LABEL description="Tet Gift Commerce API - Spring Boot Application"

WORKDIR /app

# Copy pom.xml and download dependencies first (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# ========================
# Stage 2: Runtime Stage
# ========================
FROM eclipse-temurin:21-jre-alpine

LABEL authors="QuangHuyy"
LABEL description="Tet Gift Commerce API - Runtime"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/v1/actuator/health || exit 1

# Set JVM options and run the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
