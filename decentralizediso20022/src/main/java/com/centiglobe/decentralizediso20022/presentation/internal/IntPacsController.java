package com.centiglobe.decentralizediso20022.presentation.internal;

import org.slf4j.LoggerFactory;

import java.util.Map;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;
import com.centiglobe.decentralizediso20022.application.ValidationService;
import com.centiglobe.decentralizediso20022.application.internal.IntMessageService;
import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * A controller for handling internal pacs messages
 * 
 * @author William Stackenäs
 */
@RestController
@Profile("internal")
@ApiVersion(1)
@RequestMapping("pacs")
public class IntPacsController {

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    @Value("${recipient.port}")
    private String PORT;
    
    @Autowired
    private IntMessageService msgService;

    private static final Logger LOGGER = LoggerFactory.getLogger(IntPacsController.class);

    @PostMapping("/")
    public ResponseEntity handlePacs(@RequestBody String pacs) {
        LOGGER.info("Internal cotroller handling pacs message.");
        AbstractMX mx = AbstractMX.parse(pacs);
        if (mx == null || !mx.getBusinessProcess().equals("pacs"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The entity was not a valid pacs message.");
        
        BusinessAppHdrV02 header = (BusinessAppHdrV02) mx.getAppHdr();
        try {
            int port = Integer.parseInt(PORT);
            ValidationService.validateHeaderFrom(header, port, TRUST_STORE, TRUST_PASS);
            ValidationService.validateHeaderTo(header, port, TRUST_STORE, TRUST_PASS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The entity had an invalid from or to header.");
        }
        try {
            return msgService.send(mx);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not process the message.");
        }
    }
}