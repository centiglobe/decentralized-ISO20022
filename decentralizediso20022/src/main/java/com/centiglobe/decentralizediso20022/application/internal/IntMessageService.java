package com.centiglobe.decentralizediso20022.application.internal;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.configureTruststore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * A service for sending messages
 * 
 * @author William Stacknäs
 * @author Cactu5
 */
@Profile("internal")
@Service
public class IntMessageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntMessageService.class);

    @Value("${server.servlet.context-path}")
    private String CONTEXT_PATH;

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    @Value("${recipient.port}")
    private String PORT;

    /**
     * Sends an ISO 20022 message using HTTPS to its dedicated endpoint at the
     * recipent host
     * 
     * @param mx The ISO 20022 message to send
     * @return The HTTP response sent by the recipent host
     * @throws Exception If sending the message failed
     */
    // TODO: Change ResponseEntity <code>String</code> to an appropriate class.
    public ResponseEntity<String> send(AbstractMX mx) throws Exception {
        String host = ((BusinessAppHdrV02) mx.getAppHdr()).getTo().getFIId().getFinInstnId().getNm();
        // byte[] encoded = mx.message().getBytes("utf-8");

        URI uri = new URI("https://" + host + ":" + PORT + endpointOf(mx));

        // TODO: unwrap and throw errors from the request!!!
        System.out.println("--------------IntMessageService---------------");
        System.out.println("Sending Request!");
        System.out.println("--------------IntMessageService---------------");
        ResponseEntity<String> test = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE).build().post().uri(uri)
                .bodyValue(mx).retrieve().onStatus(HttpStatus::isError, (it -> {
                    System.out.println("--------------IntMessageService---------------");
                    System.out.println("Status code:");
                    System.out.println(it.statusCode());
                    System.out.println("--------------IntMessageService---------------");
                    return Mono.error(Exception::new);
                })).toEntity(String.class).block();
        System.out.println("--------------IntMessageService---------------");
        System.out.println(test.toString());
        System.out.println("--------------IntMessageService---------------");
        return test;

        // HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        // conn.setRequestMethod("POST");
        // configureTruststore(conn, TRUST_STORE, TRUST_PASS);
        // conn.setDoOutput(true);
        // conn.setRequestProperty("Content-Length", "" + encoded.length);
        // conn.getOutputStream().write(encoded);

        // return getHttpsResponse(conn);
    }

    private ResponseEntity getHttpsResponse(HttpsURLConnection con) throws IOException {
        InputStream reader = hasErrorResponse(con) ? con.getErrorStream() : con.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(reader, "utf-8"));
        StringBuilder respReader = new StringBuilder();
        String row;
        while ((row = in.readLine()) != null) {
            respReader.append(row + "\n");
        }
        HttpHeaders headers = new HttpHeaders();
        Map<String, List<String>> conHeaders = con.getHeaderFields();
        for (String header : conHeaders.keySet()) {
            if (header != null) {
                headers.add(header, conHeaders.get(header).get(0));
            }
        }
        String body = respReader.toString();
        return ResponseEntity.status(con.getResponseCode()).headers(headers).body(body);
    }

    private boolean hasErrorResponse(HttpsURLConnection con) throws IOException {
        return con.getResponseCode() > 299;
    }

    private String endpointOf(AbstractMX mx) {
        return CONTEXT_PATH + "/v1/" + mx.getBusinessProcess() + "/";
    }
}
