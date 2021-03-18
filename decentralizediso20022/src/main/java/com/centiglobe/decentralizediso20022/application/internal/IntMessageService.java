package com.centiglobe.decentralizediso20022.application.internal;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;

import static com.centiglobe.decentralizediso20022.util.HTTPSCustomTruststore.configureTruststore;
import com.centiglobe.decentralizediso20022.util.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

@Profile("internal")
@Service
public class IntMessageService {

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
    public HttpResponse send(AbstractMX mx) throws Exception {
        String host = ((BusinessAppHdrV02)mx.getAppHdr()).getTo().getFIId().getFinInstnId().getNm();
        byte[] encoded = mx.message().getBytes("utf-8");
        URL url = new URL("https://" + host + ":8443" + endpointOf(mx));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        configureTruststore(conn, TRUST_STORE, TRUST_PASS);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Length", "" + encoded.length);
        conn.getOutputStream().write(encoded);

        return getHttpsResponse(conn);
    }

    private HttpResponse getHttpsResponse(HttpsURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
        StringBuilder respReader = new StringBuilder();
        String row;
        while ((row = in.readLine()) != null) {
            respReader.append(row + "\n");
        }
        Hashtable<String, String> headers = new Hashtable<String, String>();
        Map<String, List<String>> conHeaders = con.getHeaderFields();
        for (String header : conHeaders.keySet()) {
            if (header != null) {
                headers.put(header, conHeaders.get(header).get(0));
            }
        }
        String body = respReader.toString();
        return new HttpResponse(con.getResponseCode(), headers, body);
    }

    private String endpointOf(AbstractMX mx) {
        return "/external/v1/" + mx.getBusinessProcess();
    }
}
