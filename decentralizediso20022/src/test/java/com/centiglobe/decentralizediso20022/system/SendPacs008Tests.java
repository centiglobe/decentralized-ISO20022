package com.centiglobe.decentralizediso20022.system;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.centiglobe.decentralizediso20022.Decentralizediso20022Application;
import com.centiglobe.decentralizediso20022.util.HTTPSFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Testing sending pacs.008.001.09 messages to test the system.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
@SpringBootTest
public class SendPacs008Tests {

    @Value("${server.servlet.context-path}")
    private String CONTEXT_PATH;

    @Value("${message.empty}")
    private String EMPTY_MSG;

    @Value("${message.bad-pacs}")
    private String BAD_PACS;

    @Value("${message.bad-from-header}")
    private String BAD_FROM_HEADER;

    @Value("${message.bad-to-header}")
    private String BAD_TO_HEADER;

    @Value("${message.bad-recipient}")
    private String BAD_RECIPIENT;

    @Value("${message.bad-recipient-uri}")
    private String BAD_URI;

    @Value("${message.bad-internal-cert}")
    private String BAD_INTERNAL_CERT;

    @Value("${message.bad-external-cert}")
    private String BAD_EXTERNAL_CERT;

    @Value("${message.internal-send-failure}")
    private String INT_SEND_FAILURE;

    @Value("${message.external-send-failure}")
    private String EXT_SEND_FAILURE;

    @Value("${server.ssl.key-store}")
    private String KEY_STORE;

    @Value("${server.ssl.key-store-password}")
    private String KEY_PASS;

    @Value("${server.ssl.trust-store}")
    private String TRUST_STORE;

    @Value("${server.ssl.trust-store-password}")
    private String TRUST_PASS;

    @Autowired
    @Qualifier("secureWebClient")
    public WebClient.Builder webClientBuilder;

    static ConfigurableApplicationContext internal;
    static ConfigurableApplicationContext external;
    static String mx = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<RequestPayload>\n    <h:AppHdr xmlns:h=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">\n        <h:Fr>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>%s</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:Fr>\n        <h:To>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>%s</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:To>\n        <h:BizMsgIdr>12312312312</h:BizMsgIdr>\n        <h:MsgDefIdr>pacs.008.001.09</h:MsgDefIdr>\n        <h:CreDt>2021-03-16T22:02:04.643+01:00</h:CreDt>\n    </h:AppHdr>\n    <Doc:Document xmlns:Doc=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09\">\n        <Doc:FIToFICstmrCdtTrf>\n            <Doc:GrpHdr>\n                <Doc:MsgId>TBEXO12345</Doc:MsgId>\n                <Doc:CreDtTm>2021-03-16T22:02:04.170+01:00</Doc:CreDtTm>\n                <Doc:NbOfTxs>1</Doc:NbOfTxs>\n                <Doc:SttlmInf>\n                    <Doc:SttlmMtd>INDA</Doc:SttlmMtd>\n                </Doc:SttlmInf>\n            </Doc:GrpHdr>\n            <Doc:CdtTrfTxInf>\n                <Doc:PmtId>\n                    <Doc:EndToEndId>TBEXO12345</Doc:EndToEndId>\n                    <Doc:UETR>df4309a2-0705-481c-a714-cbd999549fd1</Doc:UETR>\n                </Doc:PmtId>\n                <Doc:IntrBkSttlmAmt Ccy=\"EUR\">100</Doc:IntrBkSttlmAmt>\n                <Doc:ChrgBr>DEBT</Doc:ChrgBr>\n                <Doc:Dbtr>\n                    <Doc:Nm>JOE DOE</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>310 Field Road, NY</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Dbtr>\n                <Doc:DbtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>FOOBARC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:DbtrAgt>\n                <Doc:CdtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>BANKANC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:CdtrAgt>\n                <Doc:Cdtr>\n                    <Doc:Nm>TEST CORP</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>Nellis ABC, NV</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Cdtr>\n            </Doc:CdtTrfTxInf>\n        </Doc:FIToFICstmrCdtTrf>\n    </Doc:Document>\n</RequestPayload>";

    @BeforeAll
    static void setUp() throws InterruptedException {
        SpringApplicationBuilder intBuild = new SpringApplicationBuilder(Decentralizediso20022Application.class);
        internal = intBuild.profiles("internal").run();

        SpringApplicationBuilder extBuild = new SpringApplicationBuilder(Decentralizediso20022Application.class);
        external = extBuild.profiles("external").run();
    }

    @Test
    void sendPacsTest() throws Exception {
        validateResponse(sendPost(String.format(mx, "localhost", "localhost")), HttpStatus.OK, "pacs.002");
    }

    @Test
    void sendGarbageTest() throws Exception {
        validateResponse(sendPost("lhjsfslkfjsdlfjsdlfgkjsgpoimv0iwmf9o"), HttpStatus.BAD_REQUEST, BAD_PACS);
    }

    @Test
    void sendInvalidToTest() throws Exception {
        validateResponse(sendPost(String.format(mx, "localhost", "self-signed.badssl.com")), HttpStatus.FORBIDDEN,
                BAD_EXTERNAL_CERT);
    }

    @Test
    void sendRevokedToTest() throws Exception {
        validateResponse(sendPost(String.format(mx, "localhost", "revoked.badssl.com")), HttpStatus.FORBIDDEN,
                BAD_EXTERNAL_CERT);
    }

    @Test
    void sendInvalidFromTest() throws Exception {
        validateResponse(sendPost(String.format(mx, "self-signed.badssl.com", "localhost")), HttpStatus.BAD_REQUEST,
                String.format(BAD_FROM_HEADER, "self-signed.badssl.com"));
    }

    @Test
    void sendEmptyTest() throws Exception {
        validateResponse(sendPost(""), HttpStatus.BAD_REQUEST, EMPTY_MSG);
    }

    @Test
    void sendToExternalEmptyTest() throws Exception {
        validateResponse(sendPost("", new URL("https://localhost:443/api/v1/pacs"), true, true), HttpStatus.BAD_REQUEST, EMPTY_MSG);
    }

    @Test
    void sendToBadEndpointTest() throws Exception {
        validateResponse(
                sendPost(String.format(mx, "localhost", "localhost"), new URL("http://localhost:8080/api/v1/fdsfsf/")),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void sendToExternalUnauthenticated() throws Exception {
        String exmsg = "bad_certificate";
        SSLHandshakeException e = assertThrows(SSLHandshakeException.class, () ->
            sendPost(String.format(mx, "localhost", "localhost"), new URL("https://localhost:443/api/v1/pacs"), false, true)
        );
        assertTrue(e.getMessage().contains(exmsg), "SSLHandshakeException with message " + e.getMessage() + " did not contain \"" + exmsg + "\".");
    }

    @Test
    void sendToUnauthenticatedExternal() throws Exception {
        String exmsg = "unable to find valid certification";
        SSLHandshakeException e = assertThrows(SSLHandshakeException.class, () ->
            sendPost(String.format(mx, "localhost", "localhost"), new URL("https://localhost:443/api/v1/pacs"), true, false)
        );
        assertTrue(e.getMessage().contains(exmsg), "SSLHandshakeException with message " + e.getMessage() + " did not contain \"" + exmsg + "\".");
    }

    @Test
    void sendToExternalAuthenticated() throws Exception {
        validateResponse(
            sendPost(String.format(mx, "localhost", "localhost"), new URL("https://localhost:443/api/v1/pacs"), true, true),
            HttpStatus.OK,
            "pacs.002"
        );
    }

    @Test
    void sendToExternalBadTo() throws Exception {
        validateResponse(
            sendPost(String.format(mx, "localhost", "self-signed.badssl.com"), new URL("https://localhost:443/api/v1/pacs"), true, true),
            HttpStatus.BAD_REQUEST,
            String.format(BAD_TO_HEADER, "self-signed.badssl.com")
        );
    }

    @Test
    void sendToExternalBadFrom() throws Exception {
        validateResponse(
            sendPost(String.format(mx, "self-signed.badssl.com", "localhost"), new URL("https://localhost:443/api/v1/pacs"), true, true),
            HttpStatus.BAD_REQUEST,
            String.format(BAD_FROM_HEADER, "self-signed.badssl.com")
        );
    }

    @Test
    void sendGarbageRecipient() throws Exception {
        String badhost = "/YHhj?)(H)%\"!%H/#";
        validateResponse(
            sendPost(String.format(mx, "localhost", badhost)),
            HttpStatus.BAD_REQUEST,
            String.format(BAD_URI, "https://" + badhost + CONTEXT_PATH + "/v1/pacs")
        );
    }

    @Test
    void sendNonexistentRecipient() throws Exception {
        String nohost = "thisdomainname.isverylong.anddoesntexist.com";
        validateResponse(
            sendPost(String.format(mx, "localhost", nohost)),
            HttpStatus.BAD_GATEWAY,
            INT_SEND_FAILURE
        );
    }

    @Test
    void sendEmptyRecipient() throws Exception {
        validateResponse(
            sendPost(String.format(mx, "localhost", "")),
            HttpStatus.BAD_REQUEST,
            String.format(BAD_RECIPIENT, "[blank]")
        );
    }

    // TODO: Is it possible to test the EXT_SEND_FAILURE error or the BAD_INTERNAL_CERT error?
    // So far they have only been tested manually

    private void validateResponse(ResponseEntity<String> resp, HttpStatus expectedStatus) {
        validateResponse(resp, expectedStatus, null);
    }

    private void validateResponse(ResponseEntity<String> resp, HttpStatus expectedStatus, String expectedMsg) {
        assertEquals(expectedStatus, resp.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, resp.getHeaders().getContentType());
        String body = resp.getBody().toString();
        if (expectedMsg != null)
            assertTrue(body.contains(expectedMsg), body + "\n\nDid not contain\n\n" + expectedMsg + "\n");
    }

    private ResponseEntity<String> sendPost(String mx) throws Exception {
        return sendPost(mx, new URL("http://localhost:8080/api/v1/pacs"));
    }

    private ResponseEntity<String> sendPost(String mx, URL url) throws Exception {
        return sendPost(mx, url, false, false);
    }

    private ResponseEntity<String> sendPost(String mx, URL url, boolean key, boolean trust) throws Exception {
        byte[] encoded = mx.getBytes("utf-8");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);

        conn.setUseCaches(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/xml");
        conn.setRequestProperty("Content-Type", "application/xml");
        if (key || trust)
            configureTruststore((HttpsURLConnection)conn, key, trust);

        conn.setRequestProperty("Content-Length", "" + encoded.length);
        OutputStream out = conn.getOutputStream();
        out.write(encoded);
        out.flush();
        out.close();

        return getHttpResponse(conn);
    }

    /**
     * Doesn't parse the response 100% correctly, but it gets the job done for these tests
     */
    private ResponseEntity<String> getHttpResponse(HttpURLConnection con) throws IOException {
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

    private boolean hasErrorResponse(HttpURLConnection con) throws IOException {
        return con.getResponseCode() > 299;
    }

    private void configureTruststore(HttpsURLConnection connection, boolean keystore, boolean truststore)
            throws Exception {
        KeyManagerFactory kmf = null;
        TrustManagerFactory tmf = null;

        if (keystore) {
            kmf = HTTPSFactory.createKeyManager(new File(KEY_STORE).getName(), KEY_PASS);
        }
        if (truststore) {
            tmf = HTTPSFactory.createTrustManager(new File(TRUST_STORE).getName(), TRUST_PASS);
        }

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(keystore ? kmf.getKeyManagers() : null, truststore ? tmf.getTrustManagers() : null, new java.security.SecureRandom());

        SSLSocketFactory socketFact = ctx.getSocketFactory();
        connection.setSSLSocketFactory(socketFact);
    }

    @AfterAll
    static void tearDown() {
        internal.close();
        external.close();
    }
}
