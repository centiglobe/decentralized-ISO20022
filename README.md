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

## Demo
The following instructions describe how to run a demo of the system using a mock bank system and the default configuration.
   1. Start the microservice and mock bank system by running `docker-compose -f docker-compose.demo.yml up --build`.
   2. Send a post request to `http://localhost:8080/api/v1/pacs` containing a Pacs.008 message in the message body.
      An example request payload can be found in `examples/demo/docker-demo.xml`.
   3. If successful the response should contain a Pacs.002 message, otherwise an error message explaining what went wrong.

## Configuration
The following instructions describe how to configure the system and integrate it into an existing bank system.
   1. Generate a Keystore and a Truststore that should be used by the system. The Keystore should contain your own institution's
      personal X.509 Certificate with a unique alias and corresponding private key. The Truststore should contain the certificates of the financial
	  institutions that are deemed as trustworthy.
   2. Navigate to `/decentralizediso20022/src/main/resources` and replace the template `keystore.p12` and `truststore.p12` with the ones
      from the previous step.
   3. Edit the configuration in the `application.properties` files. Below is a list of the properties that is configurable. Properties
      not listed here **must not be changed unless you are aware of the risks**.
	  -  `application.properties`
         - `server.ssl.key-store`: The path to your keystore. This field should always be `/src/main/resources/<keystore-filename>`.
         - `server.ssl.key-store-password`: The password of your keystore.
         - `server.ssl.keyAlias`: The alias of your personal X.509 certificate within the keystore.
         - `server.ssl.trust-store`:  The path to your truststore. This field should always be `/src/main/resources/<truststore-filename>`.
         - `server.ssl.trust-store-password`: The password of your truststore.
      - `application-internal.properties`
         - `server.port`: The TCP port that the internal API runs on. If changed and docker is used,
                          `/decentralizediso20022/docker-compose.internal.yml` must be updated to match it.
		 - `server.ssl.enabled`: Should be set to `false`, as usage of TLS in the internal API has not been sufficiently tested.
	  - `application-external.properties`
         - `server.port`: The TCP port that the external API runs on. If changed and docker is used,
                          `/decentralizediso20022/docker-compose.external.yml` must be updated to match it.
		 - `message.handler.endpoint`: The full URL of the handler that valid ISO 20022 messages should be forwarded to by the external API.

## Setup
The following instructions describe how to start up the system after configuring it.

### Using Docker
Navigate to `/decentralizediso20022` and start the microservice by running
`docker-compose -f docker-compose.external.yml -f docker-compose.internal.yml up --build`