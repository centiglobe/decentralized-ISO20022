package com.centiglobe.decentralizediso20022.system;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.centiglobe.decentralizediso20022.Decentralizediso20022Application;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Testing sending pacs.008.001.09 messages to test the system.
 * 
 * @author Cactu5
 * @author William Stackenäs
 */
@SpringBootTest
@ActiveProfiles("test")
public class SendPacs008Tests {

    @Value("${message.empty}")
    private String EMPTY_MSG;

    @Value("${message.bad-pacs}")
    private String BAD_PACS;

    @Value("${message.bad-header}")
    private String BAD_HEADER;

    @Value("${message.500}")
    private String INTERNAL_ERROR;

    @Value("${message.200}")
    private String OK;

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
    void sendPacsTest() throws IOException, InterruptedException {
        validateResponse(sendPost(String.format(mx, "localhost", "localhost")), HttpStatus.OK, OK);
    }

    @Test
    void sendGarbageTest() throws IOException, InterruptedException {
        validateResponse(sendPost("lhjsfslkfjsdlfjsdlfgkjsgpoimv0iwmf9o"), HttpStatus.BAD_REQUEST, BAD_PACS);
    }

    @Test
    void sendInvalidToTest() throws IOException, InterruptedException {
        validateResponse(sendPost(String.format(mx, "localhost", "self-signed.badssl.com")), HttpStatus.BAD_REQUEST,
                BAD_HEADER);
    }

    @Test
    void sendEmptyTest() throws IOException, InterruptedException {
        validateResponse(sendPost(""), HttpStatus.BAD_REQUEST, EMPTY_MSG);
    }

    @Test
    void sendToBadEndpointTest() throws IOException, InterruptedException {
        validateResponse(
                sendPost(String.format(mx, "localhost", "localhost"), new URL("http://localhost:8080/api/v1/fdsfsf/")),
                HttpStatus.NOT_FOUND);
    }

    private void validateResponse(ResponseEntity resp, HttpStatus expectedStatus) {
        validateResponse(resp, expectedStatus, null);
    }

    private void validateResponse(ResponseEntity resp, HttpStatus expectedStatus, String expectedMsg) {
        assertEquals(expectedStatus, resp.getStatusCode());
        assertEquals(resp.getHeaders().getContentType(), MediaType.APPLICATION_XML);
        String body = resp.getBody().toString();
        if (expectedMsg != null)
            assertTrue(body.contains(expectedMsg), body + "\n\nDid not contain\n\n" + expectedMsg + "\n");
    }

    private ResponseEntity sendPost(String mx) throws IOException {
        return sendPost(mx, new URL("http://localhost:8080/api/v1/pacs/"));
    }

    private ResponseEntity sendPost(String mx, URL url) throws IOException {
        byte[] encoded = mx.getBytes("utf-8");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);

        conn.setUseCaches(true);
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Accept", "application/xml");
        conn.setRequestProperty("Content-Type", "application/xml");

        conn.setRequestProperty("Content-Length", "" + encoded.length);
        OutputStream out = conn.getOutputStream();
        out.write(encoded);
        out.flush();
        out.close();

        return getHttpResponse(conn);
    }

    private ResponseEntity getHttpResponse(HttpURLConnection con) throws IOException {
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

    @AfterAll
    static void tearDown() {
        internal.close();
        external.close();
    }
}
