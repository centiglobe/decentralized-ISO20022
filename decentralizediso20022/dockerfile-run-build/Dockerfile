FROM maven:3.6.0-jdk-11-slim
COPY src /app/src
WORKDIR /app
COPY pom.xml pom.xml
RUN mvn -f /app/pom.xml clean package
#ENTRYPOINT ["tail", "-f", "/dev/null"]
# change image to one with maven pre-installed
# docker build -t <some tag> -f <dir/dir/Dockerfile> .
# use above instead of from docker compose.
# will probably work
