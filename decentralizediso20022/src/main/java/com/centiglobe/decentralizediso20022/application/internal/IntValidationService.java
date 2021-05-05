package com.centiglobe.decentralizediso20022.application.internal;

import java.security.cert.X509Certificate;

import com.centiglobe.decentralizediso20022.application.ValidationService;
import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * A Validation service used for validating outgoing ISO 20022 messages.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
@Profile("internal")
@Service
public class IntValidationService extends ValidationService {

    @Value("${message.bad-from-header}")
    private String BAD_FROM_HEADER;
    
    @Value("${message.bad-to-header}")
    private String BAD_TO_HEADER;

    /**
     * Validates the ISO 20022 message. May optionally validate the message against
     * a given certificate
     *
     * @param mx The message to validate
     * @param cert The optional certificate to validate the message against
     *
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid. The reason
     *                                  can be obtained via the getMessage method
     */
    public void validateMessage(AbstractMX mx, X509Certificate cert) throws IllegalArgumentException, NullPointerException {
        validateHeader((BusinessAppHdrV02) mx.getAppHdr(), cert);
    }

    /**
     * Validates that the domain nested in the From element without port number
     * is contained in the subject alternative names of the local certificate. May
     * optionally also validate the To element without port number against a given
     * certificate's subject alternative names.
     *
     * @param header The header to validate
     * @param cert The optional certificate to validate the To element against
     *
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid. The reason
     *                                  can be obtained via the getMessage method
     */
    public void validateHeader(BusinessAppHdrV02 header, X509Certificate cert) throws IllegalArgumentException, NullPointerException {
        String from = header.getFr().getFIId().getFinInstnId().getNm();
        String fromDomain = from.split(":")[0];

        if (!hasSubjectAltName(us, fromDomain))
            throw new IllegalArgumentException(String.format(BAD_FROM_HEADER, fromDomain));

        if (cert != null) {
            String to = header.getTo().getFIId().getFinInstnId().getNm();
            String toDomain = to.split(":")[0];

            if (!hasSubjectAltName(cert, toDomain))
                throw new IllegalArgumentException(String.format(BAD_TO_HEADER, toDomain));
        }
    }
}
