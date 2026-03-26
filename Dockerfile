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

# Pre-download ONNX model and tokenizer at build time to avoid runtime download issues
RUN mkdir -p /app/models && \
    apt-get update && apt-get install -y curl && \
    curl -L -o /app/models/model.onnx "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx?download=true" && \
    curl -L -o /app/models/tokenizer.json "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json?download=true" && \
    apt-get remove -y curl && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]