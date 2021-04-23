# Decentralized ISO20022 communication service
Financial Inclusion with decentralized ISO20022 as a bidirectional communication service. It consists of two components, the internal API and the external API.
The internal API should be kept private and only accessible by your own financial institution, which could be done using for example a
VPN or a firewall. Its purpose is to validate outgoing ISO 20022 messages before they are sent to a remote financial institution. The external API is
publicly available for remote financial institutions and can accept and validate incoming ISO 20022 messages before forwarding them
to an internal handler service.

## Table of Contents
 - [1. System Overview](#1-system-overview)
 - [2. Sequence Diagram](#2-sequence-diagram)
 - [3. Use In Settlement And Payments](#3-use-in-settlement-and-payments)
 - [4. Technologies](#4-technologies)
 - [5. Demo](#5-demo)
 - [6. How To Use](#6-how-to-use)
   - [6.1. Endpoints](#61-endpoints)
   - [6.2. Certificates](#62-certificates)
   - [6.3. Sending Messages](#63-sending-messages)
   - [6.4. Receiving Messages](#64-receiving-messages)
   - [6.5. Configuration](#65-configuration)
 - [7. Setup](#7-setup)
   - [7.1. Using Linux](#71-using-linux)
   - [7.2. Using Windows](#72-using-windows)
   - [7.3. Using Docker](#73-using-docker)
 - [8. Software Architecture](#8-software-architecture)


## 1. System overview
An overview of the system, using VPNs to protect the internal APIs.
![Image of the system overview](images/system-overview.png)

## 2. Sequence diagram
A sequence diagram for sending and receiving a pacs.008 message with the communication service.

![Image of a sequence diagram for sending and receiving a pacs.008 message](images/sequence-diagram-01.png)

## 3. Use In Settlement And Payments
An example use case of the communication service in payments with FX settlements on a distributed ledger, and the different ISO20022 messages that can be used for communication.
![Image of a sequence diagram in a settlement and payments use case](images/sequence-diagram-02.png)

## 4. Technologies
The technologies used in the project. 
- [Spring Boot](https://github.com/spring-projects/spring-boot) is used as the framework.
- [Prowide-ISO20022](https://github.com/prowide/prowide-iso20022) for parsing, editing, and validating ISO20022 messages.

## 5. Demo
The following instructions describe how to run a demo of the system using a mock financial institution handler (mock bank system) and the default configuration.
   1. Start the communication service and mock bank system by running `docker-compose -f docker-compose.demo.yml up --build`.
   2. Send a post request to `http://localhost:8080/api/v1/pacs` containing a `pacs.008` message in the message body.
      An example request payload can be found in `examples/demo/docker-demo.xml`.
   3. If successful the response should contain a `pacs.002` message, otherwise an error message explaining what went wrong.

## 6. How To Use
This section explains how to use the communication service.

### 6.1 Endpoints
The communication service's internal and external APIs are RESTful and have the following URI structure.
```
/api/v1/<BUSINESS-PROCESS>
```
Where <BUSINESS-PROCESS> is the message type that is accepted by the endpoint in the entity body of POST
HTTP requests. At the moment, only `pacs` is supported. Fore more details, see the [OpenAPI description](OpenAPI/decentralized-iso20022-v1.yaml).
folder.

### 6.2 Certificates
The communication service uses both TLS client and server certificates for authentication. This means that the financial institutions that
wish to communicate with each other using the communication service must add the other party's certificate into their truststore. When
adding a certificate to your truststore, make sure that you **trust it and all certificates that are signed by it to exchange payments
with your financial institution**. It is recommended that each financial institution uses a self-signed certificate, and that each financial
institution stores the self-signed certificate of each financial institution that they trust in their truststore. Trusting root certificate CAs
commonly used on the web **should be avoided**.

Generating a key- and truststore can be accomplished using for example [keytool](https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/).
If you use that page as a guide however it is **not** recommended to use the `-trustcacerts` flag. Furthermore, it is recommended to
use the PKCS12 file format (`.p12` file extension) for key- and truststores instead of the older JKS format. (`.jks` file extension)

### 6.3 Sending Messages
To send a message, start by creating a `pacs.008` message including a business header, such as `head.001`.
This message header should contain the hostnames of the sender and the recipient of the message in its `Fr` and
`To` tags. An example is available below.
```xml
<h:AppHdr xmlns:h="urn:iso:std:iso:20022:tech:xsd:head.001.001.02">
    <h:Fr>
        <h:FIId>
            <h:FinInstnId>
                <!--The sending financial institution has the hostname "example.org" and its external API runs on port 443-->
                <h:Nm>example.org</h:Nm>
            </h:FinInstnId>
        </h:FIId>
    </h:Fr>
    <h:To>
        <h:FIId>
            <h:FinInstnId>
                <!--The receiving financial institution has the hostname "example.com" and its external API runs on port 1234-->
                <h:Nm>example.com:1234</h:Nm>
            </h:FinInstnId>
        </h:FIId>
    </h:To>
    <!--Further tags here...-->
</h:AppHdr>
<!--ISO 20022 message here...-->
```
The `Fr` tag must contain the hostname of your financial institution. Specifically, it must be a subject alternative name of your
personal X.509 certificate. The `To` tag will be used by the communication service to determine the financial institution the message should
be sent to. The port numbers of both the sending and receiving financial institution's external APIs can optionally be specified, but
if they are not present they are assumed to be located on port 443.

After the message is constructed, it should be sent to your financial institutions internal API by sending a HTTP request using the
POST method to its `/api/v1/pacs` endpoint. The response can either be a `pacs.002` message indicating success, or an error message.
An example of an error message can be found below. For a list of all error statuses that can be returned by the internal and
external APIs, see the [OpenAPI description](OpenAPI/decentralized-iso20022-v1.yaml)

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>                                                            <!--The XML header-->
<error>
    <timestamp>2021/04/21 10:36:58</timestamp>                                                                    <!--The time that the error occurred-->
    <status>502</status>                                                                                          <!--The HTTP status of the error message-->
    <code>BAD GATEWAY</code>                                                                                      <!--The Reason phrase of the HTTP status-->
    <message>The remote financial institution failed to handle the message. Please try again later.</message>     <!--A detailed error message-->
</error>
```

### 6.4 Receiving Messages
The external API which receives messages from other financial institutions will only validate the messages it receives, it will not "act"
upon them. Therefore, after it has validated a message, it will expect that a "financial institution handler" will act upon it.
The URI of this handler must be specified in `application-external.properties` in accordance with [6.5. Configuration](#65-configuration).

### 6.5 Configuration
The following instructions describe how to configure the system and integrate it into an existing bank system.
   1. Generate a Keystore and a Truststore that should be used by the system. The Keystore should contain your own institution's
      personal X.509 Certificate with a unique alias and corresponding private key. The Truststore should contain the certificates of the financial
	  institutions that are deemed as trustworthy. See [6.2. Certificates](#62-certificates) for details.
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
         - `server.port`: The TCP port that the internal API runs on. If changed and docker is used, the `ports` property in
                          `/decentralizediso20022/docker-compose.internal.yml` must be updated to match it.
		 - `server.ssl.enabled`: Should be set to `false`, as usage of TLS in the internal API is experimental and support
                                 is not confirmed.
	  - `application-external.properties`
         - `server.port`: The TCP port that the external API runs on. If changed and docker is used, the `ports` property in
                          `/decentralizediso20022/docker-compose.external.yml` must be updated to match it. Note that any financial
                          institution that will be interacting with this API must be aware of its port.
		 - `message.handler.endpoint`: The full URL of the handler that valid ISO 20022 messages should be forwarded to by the external API.

## 7. Setup
The following instructions describe how to start up the system after configuring it.
Start by navigating to `/decentralizediso20022`.

### 7.1 Using Linux
Run `./mvnw spring-boot:run -Dspring-boot.run.profiles=internal` to start the
internal API and run `./mvnw spring-boot:run -Dspring-boot.run.profiles=external`
in a separate terminal window to start the external API.

### 7.2 Using Windows
Run `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=internal` to start the
internal API and run `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=external`
in a separate command prompt window to start the external API.

### 7.3 Using Docker
Start the communication service by running `docker-compose -f docker-compose.external.yml -f docker-compose.internal.yml up --build`

## 8. Software Architecture
An architecture of the software, which follows domain driven design.
![Software Architecture](images/software-architecture.png)
