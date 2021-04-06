package com.centiglobe.decentralizediso20022.presentation.external;

import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.servlet.http.HttpServletRequest;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;
import com.centiglobe.decentralizediso20022.application.external.ExtMessageService;
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

    @Value("${message.bad-pacs}")
    private String BAD_PACS;

    @Value("${message.500}")
    private String INTERNAL_ERROR;

    @Autowired
    private ExtMessageService msgService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtPacsController.class);

    /**
     * Validates an incomming pacs message before sending it to the internal
     * financial institution system
     * 
     * @param req The HTTP request that was received along with various attributes from the TLS handshake
     * @param pacs The pacs message to validate and send
     * 
     * @return The HTTP response of the sent pacs message
     * @throws SSLException if the certificate was marked as valid by Spring but could not be obtained
     */
    @PostMapping("")
    public ResponseEntity<String> handlePacs(HttpServletRequest req, @RequestBody String pacs) throws SSLException {
        MxPacs00800109 mxPacs;
        X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
        if (certs == null || certs.length < 1) {
            // This should never happen unless Spring fails to provide the certificate
            // from the handshake as an attribute
            throw new SSLException("The TLS certificate from the handshake was unavailable.");
        }
        LOGGER.info("External cotroller handling pacs message with cert " + certs[0].getSubjectX500Principal());

        mxPacs = MxPacs00800109.parse(pacs);
        try {
            return msgService.send(mxPacs, certs[0]);
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, BAD_PACS);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Throwable e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, INTERNAL_ERROR);
        }
    }
}
