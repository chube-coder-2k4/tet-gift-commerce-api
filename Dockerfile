FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# ---------- RUNTIME ----------
# Switch to Ubuntu 22.04 (Jammy) to ensure GLIBC > 2.27 for Spring AI OnnxRuntime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app


ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]