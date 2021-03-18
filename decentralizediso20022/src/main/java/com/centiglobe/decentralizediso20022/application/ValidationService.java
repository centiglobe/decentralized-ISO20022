package com.centiglobe.decentralizediso20022.application;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.configureCustom;

/**
 * A Validation service used for validating SSL certificates.
 * 
 * @author Cactu5
 */
public class ValidationService {

    /**
     * Validates the domain nested in the To element based off the domain's SSL
     * certificate. The certificate must be present in the specified truststore for
     * it to be valid.
     * 
     * To use the default truststore enter <code>NULL</code> as the argument for
     * <code>truststore</code> and <code>pwd</code>.
     * 
     * @param header     the header
     * @param truststore the truststore path
     * @param pwd        the password for the truststore
     * @throws Exception if the header is not valid
     */
    public static void validateHeaderTo(BusinessAppHdrV02 header, String truststore, String pwd) throws Exception {
        String domain = header.getTo().getFIId().getFinInstnId().getNm();

        if (truststore != null && pwd != null) {

            checkSSLCertificate("https://" + domain, truststore, pwd);

        } else {

            checkSSLCertificate("https://" + domain, null, null);

        }
    }

    /**
     * Validates the domain nested in the Fr element based off the domain's SSL
     * certificate. The certificate must be present in the specified truststore for
     * it to be valid.
     * 
     * To use the default truststore enter <code>NULL</code> as the argument for
     * <code>truststore</code> and <code>pwd</code>.
     * 
     * @param header     the header
     * @param truststore the truststore path
     * @param pwd        the password for the truststore
     * @throws Exception if the header is not valid
     */
    public static void validateHeaderFrom(BusinessAppHdrV02 header, String truststore, String pwd) throws Exception {
        String domain = header.getFr().getFIId().getFinInstnId().getNm();

        if (truststore != null && pwd != null) {

            checkSSLCertificate("https://" + domain, truststore, pwd);

        } else {

            checkSSLCertificate("https://" + domain, null, null);

        }
    }

    /**
     * Checks that the SSL certificate is valid on the specified URL. Uses the
     * specified truststore.
     * 
     * To use the default truststore enter <code>NULL</code> as the argument for
     * <code>truststore</code> and <code>pwd</code>.
     * 
     * @param urlString  the URL
     * @param truststore the truststore path
     * @param pwd        the password for the truststore
     * @throws Exception if the SSL certificate is not valid
     */
    public static void checkSSLCertificate(String urlString, String truststore, String pwd) throws Exception {
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (truststore != null) {
            configureCustom(conn, truststore, pwd);
        }

        conn.connect();
    }
}
