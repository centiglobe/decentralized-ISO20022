package com.centiglobe.decentralizediso20022.application.external;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.Exceptions;

/**
 * A service for sending incoming messages to the local
 * financial institution
 *
 * @author William Stackenäs
 */
@Profile("external")
@Service
public class ExtMessageService {

    @Value("${bank.system.endpoint}")
    private String ENDPOINT;

    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    @Autowired
    private ExtValidationService vs;

    private URI bankUri;

    /**
     * Initalizes the URI to the local financial institution
     * 
     * @throws URISyntaxException if the configured URI is malformed
     */
    @PostConstruct
    public void initBankEndpoint() throws URISyntaxException {
        bankUri = new URI(ENDPOINT);
    }
    
    /**
     * Send an ISO 20022 message to the local financial institution
     * if it is valid.
     * 
     * @param mx The ISO 20022 message to send
     * @param cert The certificate from the client to validate the message against
     * @return The HTTP response sent by the local financial institution
     * 
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid. The reason
     *                                  can be obtained via the getMessage method
     * @throws Throwable if sending the message retulted in an erroneous status code
     *                   or failed for another reason
     */
    public ResponseEntity<String> sendIncoming(AbstractMX mx, X509Certificate cert) throws Throwable {
        vs.validateMessage(mx, cert);
        try {
            return webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build().post().uri(bankUri)
                .bodyValue(mx.message()).retrieve().toEntity(String.class).block();
        } catch (Exception e) {
            throw Exceptions.unwrap(e);
        }
    }
}
