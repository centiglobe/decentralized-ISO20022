package com.centiglobe.decentralizediso20022.application.internal;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.configureTruststore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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

    /**
     * Sends an ISO 20022 message using HTTPS to its dedicated endpoint at the recipent host
     * 
     * @param mx The ISO 20022 message to send
     * @return The HTTP response sent by the recipent host
     * @throws Exception If sending the message failed
     */
    public ResponseEntity send(AbstractMX mx) throws Exception {
        String host = ((BusinessAppHdrV02)mx.getAppHdr()).getTo().getFIId().getFinInstnId().getNm();
        byte[] encoded = mx.message().getBytes("utf-8");

        // TODO: Explore possibility of dynamic port?
        URL url = new URL("https://" + host + ":8443" + endpointOf(mx));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        configureTruststore(conn, TRUST_STORE, TRUST_PASS);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", "" + encoded.length);
        conn.getOutputStream().write(encoded);

        return getHttpsResponse(conn);
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
