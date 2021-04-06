package com.centiglobe.decentralizediso20022.application.external;

import java.security.cert.X509Certificate;

import com.centiglobe.decentralizediso20022.util.ResponseMessage;
import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * A service for sending messages
 * 
 * @author William Stacknäs
 */
@Profile("external")
@Service
public class ExtMessageService {

    @Value("${message.200}")
    private String OK;

    @Autowired
    private ExtValidationService vs;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtMessageService.class);
    
    /**
     * Forward an ISO 20022 message to the private financial institution
     * if it is valid.
     * 
     * @param mx The ISO 20022 message to forward
     * @param cert The certificate from the client to validate the message against
     * @return The HTTP response sent by the private financial institution
     * 
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid
     * @throws Throwable If forwarding the message failed
     */
    public ResponseEntity<String> send(AbstractMX mx, X509Certificate cert) throws Throwable {
        vs.validateMessage(mx, cert);

        // TODO: Send message to financial institution using a configured endpoint
        LOGGER.info("The following ISO 20022 message was sent to the Bank:\n" + mx.message());
        return ResponseMessage.generateSuccess(OK);
    }
}
