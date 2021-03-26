package com.centiglobe.decentralizediso20022.config;

import javax.net.ssl.TrustManagerFactory;

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
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.NettySslUtils;
import reactor.netty.http.client.HttpClient;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.createTrustManager;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * The configuration class for the project.
 * 
 * @author Cactu5
 * @author William Stacknäs
 */
@Configuration
@ComponentScan({ "com.centiglobe.decentralizediso20022" })
public class DecentralizedISO20022Config extends WebMvcConfigurationSupport {

    @Autowired
    @Lazy
    private ReactorClientHttpConnector reactorClient;

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
     * @throws KeyStoreException                  if keystore is the problem
     * @throws NoSuchAlgorithmException           if the algorithm does not exist
     * @throws CertificateException               if there is a problem with the
     *                                            certificate
     * @throws InvalidAlgorithmParameterException if the algorithm is invalid or
     *                                            inappropriate
     * @throws IOException                        if any problems occure while
     *                                            reading the truststore
     */
    @Bean
    public ReactorClientHttpConnector getReactorClientConfig() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, InvalidAlgorithmParameterException, IOException {
        TrustManagerFactory tm = createTrustManager(TRUST_STORE, TRUST_PASS);

        SSLFactory sslFactory = SSLFactory.builder().withTrustMaterial(tm).build();

        SslContext sslContext = NettySslUtils.forClient(sslFactory).build();
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