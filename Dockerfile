# Build
FROM maven:3.6.0-jdk-11-slim AS build
ARG PROJ_FOLDER
WORKDIR /app
COPY ${PROJ_FOLDER}/src /app/src
COPY ${PROJ_FOLDER}/pom.xml /app/pom.xml
RUN mvn -B -f /app/pom.xml clean package

# Run
FROM openjdk:11
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]
