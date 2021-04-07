package com.centiglobe.decentralizediso20022.presentation.internal;

import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;
import com.centiglobe.decentralizediso20022.application.internal.IntMessageService;
import com.centiglobe.decentralizediso20022.util.Exceptions;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;

/**
 * A controller for handling internal pacs messages
 * 
 * @author William Stackenäs
 * @author Cactu5
 */
@RestController
@Profile("internal")
@ApiVersion(1)
@RequestMapping("pacs")
public class IntPacsController {

    @Value("${message.bad-pacs}")
    private String BAD_PACS;

    @Value("${message.bad-internal-cert}")
    private String BAD_INTERNAL_CERT;

    @Value("${message.bad-external-cert}")
    private String BAD_EXTERNAL_CERT;

    @Value("${message.internal-send-failure}")
    private String SEND_FAILURE;

    @Autowired
    private IntMessageService msgService;

    private static final Logger LOGGER = LoggerFactory.getLogger(IntPacsController.class);

    /**
     * Validates an incomming pacs message before sending it to the recipient
     * financial institution
     * 
     * @param pacs The pacs message to validate and send
     * @return The HTTP response of the sent pacs message
     */
    @PostMapping("")
    public ResponseEntity<String> handlePacs(@RequestBody String pacs) throws Throwable {
        LOGGER.info("Internal cotroller handling pacs message.");
        MxPacs00800109 mxPacs = MxPacs00800109.parse(pacs);
        try {
            return msgService.send(mxPacs);
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BAD_PACS);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (WebClientRequestException | SSLHandshakeException e) {
            Throwable cause;
            if ((cause = Exceptions.getCauseOfClass(e, SSLHandshakeException.class)) != null) {
                LOGGER.warn(cause.getMessage());
                if (cause.getMessage().contains("bad_certificate")) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, BAD_INTERNAL_CERT);
                }
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, BAD_EXTERNAL_CERT);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, SEND_FAILURE);
        }
    }
}
