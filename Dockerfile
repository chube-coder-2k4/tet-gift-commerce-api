FROM maven:3.9-amazoncorretto-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# ---------- RUNTIME ----------
# Switch from alpine to standard image to support glibc for Spring AI / DJL native libs
FROM amazoncorretto:21

WORKDIR /app


ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]