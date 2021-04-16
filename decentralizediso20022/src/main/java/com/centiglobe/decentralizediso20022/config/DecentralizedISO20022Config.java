package com.centiglobe.decentralizediso20022.config;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import com.centiglobe.decentralizediso20022.util.HTTPSFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * The configuration class for the project.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
@Configuration
@ComponentScan({ "com.centiglobe.decentralizediso20022" })
public class DecentralizedISO20022Config extends WebMvcConfigurationSupport {

    @Autowired
    @Lazy
    private ReactorClientHttpConnector reactorClient;

    @Value("${server.ssl.key-store}")
    private String KEY_STORE;

    @Value("${server.ssl.key-store-password}")
    private String KEY_PASS;

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    /**
     * Returns an unconfigured <code>WebClient</code>.
     * 
     * @return the unconfigured <code>WebClient</code>
     */
    @Bean
    @Qualifier("defaultWebClientBuilder")
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * 
     * Generates a secure <code>ReactorClientHttpConnector</code>.
     * 
     * @return the secure <code>ReactorClientHttpConnector</code>
     * @throws KeyStoreException                  if the keystore is the problem
     * @throws NoSuchAlgorithmException           if the algorithm does not exist
     * @throws CertificateException               if there is a problem with the
     *                                            certificate
     * @throws InvalidAlgorithmParameterException if the algorithm is invalid or
     *                                            inappropriate
     * @throws IOException                        if any problems occur while
     *                                            reading the truststore
     * @throws UnrecoverableKeyException          if the keys could not be recovered
     *                                            from the keystore
     */
    @Bean
    public ReactorClientHttpConnector getReactorClientConfig() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, InvalidAlgorithmParameterException, IOException, UnrecoverableKeyException {
        // extract the key store paths relative to the resource folder.
        String keystore = new File(KEY_STORE).getName();
        String truststore = new File(TRUST_STORE).getName();

        KeyManagerFactory kmf = HTTPSFactory.createKeyManager(keystore, KEY_PASS);
        TrustManagerFactory tmf = HTTPSFactory.createTrustManager(truststore, TRUST_PASS);

        SslContext sslContext = SslContextBuilder.forClient()
            .keyManager(kmf)
            .trustManager(tmf)
            .build();
        HttpClient httpClient = HttpClient.create().secure(sslSpec -> sslSpec.sslContext(sslContext));
        return new ReactorClientHttpConnector(httpClient);
    }

    /**
     * Returns a secure <code>WebClient</code>.
     * 
     * @return the secure <code>WebClient</code>
     */
    @Bean
    @Qualifier("secureWebClient")
    public WebClient.Builder getSecureWebClient() {
        return WebClient.builder().clientConnector(reactorClient);
    }

    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVerRequestMappingHandlerMapping();
    }
}