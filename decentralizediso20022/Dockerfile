FROM openjdk:11
ARG JAR_FILE=target/*.jar
ARG STORES_FOLDER=project-stores
COPY ${JAR_FILE} app.jar
COPY ${STORES_FOLDER} ${STORES_FOLDER}
ENTRYPOINT [ "java", "-jar", "/app.jar" ]