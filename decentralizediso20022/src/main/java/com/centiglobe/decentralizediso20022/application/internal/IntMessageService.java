package com.centiglobe.decentralizediso20022.application.internal;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * A service for sending outgoing messages to another
 * financial institution
 * 
 * @author William Stackenäs
 * @author Cactu5
 */
@Profile("internal")
@Service
public class IntMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntMessageService.class);

    @Value("${server.servlet.context-path}")
    private String CONTEXT_PATH;

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    @Value("${message.bad-recipient}")
    private String BAD_RECIPIENT;

    @Value("${message.bad-recipient-uri}")
    private String BAD_URI;

    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    @Autowired
    private IntValidationService vs;

    /**
     * Sends an ISO 20022 message, if it is valid, using HTTPS to its dedicated endpoint at the
     * recipent host and returns the response returned, if it was valid.
     *
     * @param mx The ISO 20022 message to send
     * @return The HTTP response sent by the recipent host
     *
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid. The reason
     *                                  can be obtained via the getMessage method
     * @throws XMLParseException if the response received from the remote financial
     *                           institution could not be understood or validated
     * @throws Throwable if, for example, a secure TLS session could not be
     *                   established with the recipient
     */
    public ResponseEntity<String> sendOutgoing(AbstractMX mx) throws Throwable {
        vs.validateMessage(mx, null);
        
        URI uri = endpointOf(mx);
        ResponseEntity<String> response;
        try {
            response = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build().post().uri(uri)
                .bodyValue(mx.message()).retrieve().onStatus(HttpStatus::isError, (resp -> {
                    if (resp.statusCode().is4xxClientError()) {
                        // If the response is a 4xx error, it means the internal component mistakenly
                        // validated the message. (Or the external component mistakenly flagged it)
                        LOGGER.error("Received a " + resp.statusCode() + " status from the recipient.");
                    } else {
                        LOGGER.debug("Received a " + resp.statusCode() + " status from the recipient.");
                    }
                    return Mono.empty();
                })).toEntity(String.class).block();
        } catch (Exception e) {
            throw Exceptions.unwrap(e);
        }

        // TODO: When error messages are updated to PACS.002 messages instead of
        // custom formats, MxPacs00200111.parse() should be called instead.
        try {
            validateXml(response.getBody());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.debug("Bad response:\n" + response.getBody());
            throw new XMLParseException("Received a non-XML response.");
        }
        return response;
    }

    /**
     * Obtains the URI endpoint that the given ISO 20022 message should be sent to
     * It is based on the {@link BusinessAppHdrV02}'s' To element and the message type
     * 
     * @param mx The message whose URI endpoint should be obtained
     * 
     * @return The URI that the message should be sent to
     * @throws IllegalArgumentException if the full URI was malformed
     */
    private URI endpointOf(AbstractMX mx) {
        String host = "[blank]";
        String to;
        String uri = null;
        try {
            to = ((BusinessAppHdrV02) mx.getAppHdr()).getTo().getFIId().getFinInstnId().getNm();
            if (to.isBlank())
                throw new Exception("Blank hostname");
            host = to;
            uri = "https://" + host + CONTEXT_PATH + "/v1/" + mx.getBusinessProcess();
            return new URI(uri);
        } catch (URISyntaxException e) {
            LOGGER.error(String.format(BAD_URI, uri));
            throw new IllegalArgumentException(String.format(BAD_URI, uri));
        } catch (Exception e) {
            LOGGER.error(String.format(BAD_RECIPIENT, host));
            throw new IllegalArgumentException(String.format(BAD_RECIPIENT, host));
        }
    }

    private void validateXml(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        reader.parse(new InputSource(new StringReader(xmlString)));
    }
}
