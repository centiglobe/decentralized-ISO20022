# Build
FROM maven:3.6.0-jdk-11-slim AS build
ARG PROJ_FOLDER
RUN echo ${PROJ_FOLDER}
COPY ${PROJ_FOLDER}/src /app/src
WORKDIR /app
COPY ${PROJ_FOLDER}/pom.xml ${PROJ_FOLDER}/pom.xml
RUN mvn -B -f /app/pom.xml clean package

# Run
FROM openjdk:11
ARG STORES_FOLDER=project-stores
COPY --from=build /app/target/*.jar app.jar
COPY ${STORES_FOLDER} ${STORES_FOLDER}
ENTRYPOINT [ "java", "-jar", "/app.jar" ]