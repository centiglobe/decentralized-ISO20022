package com.centiglobe.decentralizediso20022.application.external;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("external")
@Service
public class ExtMessageService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtMessageService.class);
    
    /**
     * 
     * @param mx The ISO 20022 message to send
     */
    public void send(AbstractMX mx) throws Exception {
        // TODO: Send message to financial institution using configured endpoint
        LOGGER.info("The following ISO 20022 message was sent to the Bank:\n" + mx.message());
    }
}
