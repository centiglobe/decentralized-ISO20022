package com.centiglobe.decentralizediso20022.presentation.external;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;
import com.centiglobe.decentralizediso20022.application.ValidationService;
import com.centiglobe.decentralizediso20022.application.external.ExtMessageService;
import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * A controller for handling external pacs messages
 * 
 * @author William Stackenäs
 */
@RestController
@Profile("external")
@ApiVersion(1)
@RequestMapping("pacs")
public class ExtPacsController {

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    @Value("${recipient.port}")
    private String PORT;
    
    @Autowired
    private ExtMessageService msgService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtPacsController.class);

    @PostMapping("/")
    public ResponseEntity handlePacs008(@RequestBody String pacs) throws UnsupportedEncodingException {
        LOGGER.info("External cotroller handling pacs message");
        String decodedPacs = URLDecoder.decode(pacs, StandardCharsets.UTF_8.name());

        AbstractMX mx = AbstractMX.parse(decodedPacs);
        if (mx == null || !mx.getBusinessProcess().equals("pacs"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The entity was not a valid pacs message.");
        
        BusinessAppHdrV02 header = (BusinessAppHdrV02) mx.getAppHdr();
        try {
            // Extract path relative to the classpath
            String truststore = new File(TRUST_STORE).getName();
            int port = Integer.parseInt(PORT);
            ValidationService.validateHeaderFrom(header, port, truststore, TRUST_PASS);
            ValidationService.validateHeaderTo(header, port, truststore, TRUST_PASS);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The entity had an invalid from or to header.");
        }
        try {
            msgService.send(mx);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not process the message.");
        }
        // TODO: Respond with the response from the bank service
        throw new ResponseStatusException(HttpStatus.OK, "The message was processed.");
    }
}
