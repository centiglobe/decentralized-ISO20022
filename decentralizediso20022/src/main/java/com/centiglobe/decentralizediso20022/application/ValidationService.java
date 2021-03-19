package com.centiglobe.decentralizediso20022.application;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.configureTruststore;

/**
 * A Validation service used for validating SSL certificates.
 * 
 * @author Cactu5
 */
public class ValidationService {

    /**
     * Validates the domain nested in the To element based off the domain's SSL
     * certificate. The certificate must also be present in the specified truststore
     * for it to be valid.
     * 
     * @param header     the header
     * @param truststore the truststore path
     * @param pwd        the password for the truststore
     * @throws Exception if the header is not valid
     */
    public static void validateHeaderTo(BusinessAppHdrV02 header, String truststore, String pwd) throws Exception {
        String domain = header.getTo().getFIId().getFinInstnId().getNm();

        if (truststore != null && pwd != null) {
            checkSSLCertificate("https://" + domain + ":8443", truststore, pwd);

        } else {
            throw new Exception("The truststore and password cannot be null.");

        }
    }

    /**
     * Validates the domain name nested in the Fr element based off the domain's SSL
     * certificate. The certificate must be present in the specified truststore for
     * it to be valid.
     * 
     * @param header         the header
     * @param truststorePath the truststore path
     * @param pwd            the password for the truststore
     * @throws Exception if the header is not valid
     */
    public static void validateHeaderFrom(BusinessAppHdrV02 header, String truststorePath, String pwd)
            throws Exception {
        String domain = header.getFr().getFIId().getFinInstnId().getNm();

        if (truststorePath != null && pwd != null) {
            checkSSLCertificate("https://" + domain + ":8443", truststorePath, pwd);

        } else {
            throw new Exception("The truststore and password cannot be null.");

        }
    }

    /**
     * Checks that the SSL certificate is valid on the specified domain name. Uses
     * the specified truststore.
     * 
     * @param urlString      the URL
     * @param truststorePath the truststore path
     * @param pwd            the password for the truststore
     * @throws Exception if the SSL certificate is not valid
     */
    public static void checkSSLCertificate(String urlString, String truststorePath, String pwd) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (truststorePath != null && pwd != null) {
            configureTruststore(conn, truststorePath, pwd);
        }

        conn.connect();
    }
}
