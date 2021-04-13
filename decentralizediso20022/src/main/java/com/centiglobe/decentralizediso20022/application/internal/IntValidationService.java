package com.centiglobe.decentralizediso20022.application.internal;

import java.security.cert.X509Certificate;

import com.centiglobe.decentralizediso20022.application.ValidationService;
import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import org.cryptacular.util.CertUtil;
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
     * Validates that the domain nested in the From element is equal to the common name
     * of the local certificate. May optionally also validate the To element against a
     * given certificate's common name.
     *
     * @param header The header to validate
     * @param cert The optional certificate to validate the To element against
     *
     * @throws NullPointerException if the given message is null or lacks fields
     * @throws IllegalArgumentException if the given message is not valid. The reason
     *                                  can be obtained via the getMessage method
     */
    public void validateHeader(BusinessAppHdrV02 header, X509Certificate cert) throws IllegalArgumentException, NullPointerException {
        String fromDomain = header.getFr().getFIId().getFinInstnId().getNm();
        String ourCommonName = CertUtil.subjectCN(us);

        if (!fromDomain.equals(ourCommonName))
            throw new IllegalArgumentException(String.format(BAD_FROM_HEADER, fromDomain, ourCommonName));

        if (cert != null) {
            String toDomain = header.getTo().getFIId().getFinInstnId().getNm();
            String theirCommonName = CertUtil.subjectCN(cert);

            if (!toDomain.equals(theirCommonName))
                throw new IllegalArgumentException(String.format(BAD_TO_HEADER, toDomain, theirCommonName));
        }
    }
}
