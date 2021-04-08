FROM maven:3.6.0-jdk-11-slim
COPY src /app/src
WORKDIR /app
COPY pom.xml pom.xml
RUN mvn -f /app/pom.xml clean package
