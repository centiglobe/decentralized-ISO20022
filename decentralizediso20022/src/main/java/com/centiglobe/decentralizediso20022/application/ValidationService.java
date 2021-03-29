package com.centiglobe.decentralizediso20022.application;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.Exceptions;

/**
 * A Validation service used for validating SSL certificates.
 * 
 * @author Cactu5
 */
@Service
public class ValidationService {
    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    /**
     * Checks the certificate of the specified URL address.
     * 
     * @param urlString
     * @throws Throwable
     */
    public void checkCertificate(String urlString) throws Throwable {
        if (webClientBuilder == null) {
            throw new Exception("The webclient cannot be null.");
        }

        try {
            webClientBuilder.build().get().uri(urlString).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            throw Exceptions.unwrap(e);
        }
    }

    /**
     * Validates the domains nested in the To and Fr element based off the domain's
     * SSL certificate. The certificate must also be present in the specified
     * truststore for it to be valid.
     * 
     * @param header the header
     * @param port   the port of the domain name
     * @throws Throwable if anything goes wrong
     */
    public void validateHeader(BusinessAppHdrV02 header, int port) throws Throwable {
        validateHeaderTo(header, port);
        validateHeaderFrom(header, port);
    }

    /**
     * Validates the domain nested in the To element based off the domain's SSL
     * certificate. The certificate must also be present in the specified truststore
     * for it to be valid.
     * 
     * @param header the header
     * @param port   the port of the domain name
     * @throws Throwable
     */
    public void validateHeaderTo(BusinessAppHdrV02 header, int port) throws Throwable {
        String domain = header.getTo().getFIId().getFinInstnId().getNm();

        checkCertificate("https://" + domain + ":" + port);
    }

    /**
     * Validates the domain name nested in the Fr element based off the domain's SSL
     * certificate. The certificate must be present in the specified truststore for
     * it to be valid.
     * 
     * @param header the header
     * @param port   the port of the domain name
     * @throws Throwable
     */
    public void validateHeaderFrom(BusinessAppHdrV02 header, int port) throws Throwable {
        String domain = header.getFr().getFIId().getFinInstnId().getNm();

        checkCertificate("https://" + domain + ":" + port);
    }
}
