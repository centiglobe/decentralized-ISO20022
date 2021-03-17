@echo off
cd decentralizediso20022
start mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=internal
start mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=external
