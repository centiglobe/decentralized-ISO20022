package com.centiglobe.decentralizediso20022.application;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * A Validation service used for validating ISO 20022 messages.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
@Service
public class ValidationService {

    @Value("${server.ssl.key-store}")
    private String KEY_STORE;

    @Value("${server.ssl.key-store-password}")
    private String KEY_PASS;

    @Value("${server.ssl.keyAlias}")
    private String KEY_ALIAS;

    protected X509Certificate us;
    
    /**
     * Initalizes the validation service with the local certificate in the keystore
     * 
     * @throws Exception if the certificate could not be obtained
     */
    @PostConstruct
    public void initCertificate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(new File(KEY_STORE), KEY_PASS.toCharArray());
        us = (X509Certificate) keyStore.getCertificate(KEY_ALIAS);
    }
}
