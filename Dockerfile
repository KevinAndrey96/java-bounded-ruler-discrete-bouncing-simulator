# Stage 1: compile, run the JUnit 5 suite and package the executable jar
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn -B -q org.apache.maven.plugins:maven-dependency-plugin:3.6.1:go-offline

COPY src ./src
COPY scenarios ./scenarios
RUN mvn -B -q package

# Stage 2: minimal non-root runtime with the reference scenarios preloaded
FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system simulator \
    && useradd --system --gid simulator --home-dir /app --shell /usr/sbin/nologin simulator

WORKDIR /app

COPY --from=builder /build/target/bounded-ruler-simulator.jar ./simulator.jar
COPY scenarios ./scenarios
RUN mkdir -p /app/output && chown -R simulator:simulator /app

USER simulator

ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "simulator.jar"]
CMD ["--length", "5", "--start", "4", "--direction", "1", "--steps", "4"]
