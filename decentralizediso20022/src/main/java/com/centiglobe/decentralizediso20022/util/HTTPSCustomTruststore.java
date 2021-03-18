package com.centiglobe.decentralizediso20022.util;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * A utility class for configuring a custom keystore.
 * 
 * <a href=
 * "https://stackoverflow.com/questions/50359169/how-can-my-library-with-built-in-ssl-certificate-also-allow-use-with-default-cer">Original</a>
 */
public class HTTPSCustomTruststore {

    /**
     * Configures a custom truststore.
     * 
     * @param connection the connection the truststore will used for
     * @param truststore the truststore location
     * @param pwd        the truststore password
     * @throws Exception if any problems occur
     */
    public static void configureCustom(HttpsURLConnection connection, String truststore, String pwd) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keystore = KeyStore.getInstance("JKS");
        InputStream keystoreStream = HTTPSCustomTruststore.class.getClassLoader().getResourceAsStream(truststore);
        keystore.load(keystoreStream, pwd.toCharArray());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, trustManagers, new java.security.SecureRandom());

        SSLSocketFactory socketFact = context.getSocketFactory();
        connection.setSSLSocketFactory(socketFact);
    }
}
