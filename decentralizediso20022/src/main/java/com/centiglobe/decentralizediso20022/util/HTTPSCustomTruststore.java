package com.centiglobe.decentralizediso20022.util;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertPathBuilder;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.util.EnumSet;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * A utility class for configuring a the truststore for a HTTPS connection.
 * 
 */
public class HTTPSCustomTruststore {

    // https://stackoverflow.com/a/38523104/13820107
    /**
     * Configures a custom truststore. The default truststore can be used if
     * <code>truststorePath</code> is null. The default truststore does not work
     * well.
     * 
     * @param connection     the connection the truststore will used for
     * @param truststorePath the truststore path
     * @param pwd            the truststore password
     * @throws Exception if any problems occur
     */
    public static void configureTruststore(HttpsURLConnection connection, String truststorePath, String pwd)
            throws Exception {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream keystoreStream = HTTPSCustomTruststore.class.getClassLoader().getResourceAsStream(truststorePath);
        keystore.load(keystoreStream, pwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // initialize certification path checking for the offered certificates and
        // revocation checks against CLRs
        CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
        PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
        rc.setOptions(EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS, // prefer CLR over OCSP
                PKIXRevocationChecker.Option.SOFT_FAIL, PKIXRevocationChecker.Option.NO_FALLBACK));
                // TODO: Remove SOFT_FAIL and change to ONLY_END_ENTITY
        // don't fall back to OCSP checking

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(keystore, new X509CertSelector());
        pkixParams.addCertPathChecker(rc);

        tmf.init(new CertPathTrustManagerParameters(pkixParams));

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

        SSLSocketFactory socketFact = ctx.getSocketFactory();
        connection.setSSLSocketFactory(socketFact);
    }
}
