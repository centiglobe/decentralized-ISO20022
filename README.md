# Decentralized ISO20022
Financial Inclusion with decentralized ISO20022 as a microservice.

## Technologies
The technologies used in the project. 
- [Spring Boot](https://github.com/spring-projects/spring-boot) for the framework.
- [Prowide-ISO20022](https://github.com/prowide/prowide-iso20022) for parsing, creating and manipulating ISO20022 messages.

## System overview
An overview of the system.
![Image of the system overview](images/system-overview.png)

## Sending and recieving a pacs.008 message
A sequence diagram for sending and recieving a pacs.008 message with the microservice. Communication service represents an instance of the microservice.

![Image of a sequence diagram for sending and recieving a pacs.008 message](images/sequence-diagram-01.png)

## Use in settlement and payments
An example use case of the microservice in payments with FX settlements on a distributed ledger, and the different ISO20022 messages that can be used for communication.
![Image of a sequence diagram in a settlement and payments use case](images/sequence-diagram-02.png)

## Build Instructions
The following instructions describe how to set up the system.
   1. Navigate to `/decentralizediso20022`.
   2. Run `./mvnw clean package`
   3. The environment variables can be customized in the docker-compose files.
   4. Start docker containers. Start both docker containers the same time with `docker-compose -f docker-compose.external.yml -f docker-compose.internal.yml up --build`
   5. Or start the desired container with e.g. `docker-compose -f docker-compose.internal.yml up --build`