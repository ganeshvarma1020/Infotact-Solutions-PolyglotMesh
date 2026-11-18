# Build Stage: Compile and Package with GraalVM JDK 21
FROM ghcr.io/graalvm/jdk-community:21 AS builder
WORKDIR /workspace

RUN microdnf install -y maven findutils && microdnf clean all

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime Stage: Isolated Production Image with Non-Root User
FROM ghcr.io/graalvm/jdk-community:21
RUN groupadd -r meshgroup && useradd -r -g meshgroup meshuser
WORKDIR /app

COPY --from=builder /workspace/target/polyglotmesh-1.0.0.jar /app/app.jar
RUN chown -R meshuser:meshgroup /app

USER meshuser
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
