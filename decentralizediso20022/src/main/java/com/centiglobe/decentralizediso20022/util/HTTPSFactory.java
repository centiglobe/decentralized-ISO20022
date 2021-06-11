package com.centiglobe.decentralizediso20022.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.util.EnumSet;

import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * A utility class for configuring a the truststore for a HTTPS connection.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
public class HTTPSFactory {

    /**
     * Creates a key manager and configures security rules. Parameters cannot be
     * <code>NULL</code>.
     * 
     * @param keystorePath the path to the keystore
     * @param password       the password for the keystore
     * @return The key manger
     * @throws KeyStoreException                  if there is a problem with the
     *                                            keystore
     * @throws IOException                        if there is an problem reading the
     *                                            keystore
     * @throws CertificateException               if there is a problem with any
     *                                            certificate
     * @throws NoSuchAlgorithmException           if the algorithm does not exist
     * @throws UnrecoverableKeyException          if the keys could not be recovered
     */
    public static KeyManagerFactory createKeyManager(String keystorePath, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableKeyException {
        if (keystorePath == null || password == null) {
            throw new KeyStoreException("Keystore and password cannot be NULL.");
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream keystoreStream = HTTPSFactory.class.getClassLoader().getResourceAsStream(keystorePath);
        keyStore.load(keystoreStream, password.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("PKIX");
        kmf.init(keyStore, password.toCharArray());

        return kmf;
    }
    
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

        InputStream keystoreStream = HTTPSFactory.class.getClassLoader().getResourceAsStream(truststorePath);
        keystore.load(keystoreStream, password.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        // Note: Since only self-signed certificates should be used, revocation checks are effectively unneeded.
	// However, it is still done here in case there is a legitimate reason to use certificate chains.
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
}
