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

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import java.net.URI;

/**
 * A service for sending messages
 * 
 * @author William Stacknäs
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

    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    @Value("${recipient.port}")
    private String PORT;

    /**
     * Sends an ISO 20022 message using HTTPS to its dedicated endpoint at the
     * recipent host and returns the response returned, regardless of its status
     * 
     * @param mx The ISO 20022 message to send
     * @return The HTTP response sent by the recipent host
     * @throws Exception If sending the message failed
     */
    public ResponseEntity<String> send(AbstractMX mx) throws Throwable {
        String host;
        try {
            host = ((BusinessAppHdrV02) mx.getAppHdr()).getTo().getFIId().getFinInstnId().getNm();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to retrieve the recipient from the message.");
        }
        URI uri = new URI("https://" + host + ":" + PORT + endpointOf(mx));

        try {
            return webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build().post().uri(uri)
                .bodyValue(mx.message()).retrieve().onStatus(HttpStatus::isError, (it -> {
                    if (it.statusCode().is4xxClientError()) {
                        // If the response is a 4xx error, it means the internal component mistakenly
                        // validated the message. (Or the external component mistakenly flagged it)
                        LOGGER.error("Received a " + it.statusCode() + " status from the recipent.");
                    } else {
                        LOGGER.debug("Received a " + it.statusCode() + " status from the recipent.");
                    }
                    return Mono.empty();
                })).toEntity(String.class).block();
        } catch (Exception e) {
            throw Exceptions.unwrap(e);
        }
    }

    private String endpointOf(AbstractMX mx) {
        return CONTEXT_PATH + "/v1/" + mx.getBusinessProcess() + "/";
    }
}
