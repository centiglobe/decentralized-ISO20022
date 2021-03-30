package com.centiglobe.decentralizediso20022.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateException;
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
 * @author Cactu5
 */
public class HTTPSCustomTruststore {
    /**
     * Creates a trust manager and configures security rules. Parameters cannot be
     * <code>NULL</code>.
     * 
     * @param truststorePath the path to the truststore
     * @param password       the password for the truststore
     * @return The trust manger
     * @throws KeyStoreException                  if there is a problem with the
     *                                            keystore
     * @throws IOException                        if there is an problem reading the
     *                                            keystore
     * @throws CertificateException               if there is a problem with any
     *                                            certificate
     * @throws NoSuchAlgorithmException           if the algorithm does not exist
     * @throws InvalidAlgorithmParameterException if the algorithm is invalid
     */
    public static TrustManagerFactory createTrustManager(String truststorePath, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            InvalidAlgorithmParameterException {
        if (truststorePath == null || password == null) {
            throw new KeyStoreException("Truststore and password cannot be NULL.");
        }

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream keystoreStream = HTTPSCustomTruststore.class.getClassLoader().getResourceAsStream(truststorePath);
        keystore.load(keystoreStream, password.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // initialize certification path checking for the offered certificates and
        // revocation checks against CLRs
        CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
        PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
        rc.setOptions(EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS, // prefer CLR over OCSP
                PKIXRevocationChecker.Option.SOFT_FAIL, PKIXRevocationChecker.Option.NO_FALLBACK));
        // TODO: Remove SOFT_FAIL and change to ONLY_END_ENTITY
        // and don't fall back to OCSP checking.

        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(keystore, new X509CertSelector());
        pkixParams.addCertPathChecker(rc);

        tmf.init(new CertPathTrustManagerParameters(pkixParams));

        return tmf;
    }

    /**
     * 
     * This should be removed once IntMessageService.java has been ported to using
     * <code>WebClient</code>
     * 
     * @param connection     the https connection
     * @param truststorePath the path to the truststore
     * @param pwd            the password
     * @throws Exception if a problem occurs
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
