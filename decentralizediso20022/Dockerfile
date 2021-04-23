# Build
FROM maven:3.6.0-jdk-11-slim AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app/pom.xml
RUN mvn -B -Dmaven.test.skip=true -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn -f /app/pom.xml clean package

# Run
FROM openjdk:11
ARG STORES_FOLDER=project-stores
COPY --from=build /app/target/*.jar app.jar
COPY ${STORES_FOLDER} ${STORES_FOLDER}
ENTRYPOINT [ "java", "-jar", "/app.jar" ]
