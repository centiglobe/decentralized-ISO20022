package com.centiglobe.decentralizediso20022.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.centiglobe.decentralizediso20022.Decentralizediso20022Application;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Testing sending a pacs.008.001.09 message to test the system.
 * 
 * @author Cactu5
 */
@TestInstance(Lifecycle.PER_CLASS)
public class SendPacs008Tests {
    ConfigurableApplicationContext internal;
    ConfigurableApplicationContext external;
    static String mx = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<RequestPayload>\n    <h:AppHdr xmlns:h=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">\n        <h:Fr>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>localhost</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:Fr>\n        <h:To>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>localhost</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:To>\n        <h:BizMsgIdr>12312312312</h:BizMsgIdr>\n        <h:MsgDefIdr>pacs.008.001.09</h:MsgDefIdr>\n        <h:CreDt>2021-03-16T22:02:04.643+01:00</h:CreDt>\n    </h:AppHdr>\n    <Doc:Document xmlns:Doc=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09\">\n        <Doc:FIToFICstmrCdtTrf>\n            <Doc:GrpHdr>\n                <Doc:MsgId>TBEXO12345</Doc:MsgId>\n                <Doc:CreDtTm>2021-03-16T22:02:04.170+01:00</Doc:CreDtTm>\n                <Doc:NbOfTxs>1</Doc:NbOfTxs>\n                <Doc:SttlmInf>\n                    <Doc:SttlmMtd>INDA</Doc:SttlmMtd>\n                </Doc:SttlmInf>\n            </Doc:GrpHdr>\n            <Doc:CdtTrfTxInf>\n                <Doc:PmtId>\n                    <Doc:EndToEndId>TBEXO12345</Doc:EndToEndId>\n                    <Doc:UETR>df4309a2-0705-481c-a714-cbd999549fd1</Doc:UETR>\n                </Doc:PmtId>\n                <Doc:IntrBkSttlmAmt Ccy=\"EUR\">100</Doc:IntrBkSttlmAmt>\n                <Doc:ChrgBr>DEBT</Doc:ChrgBr>\n                <Doc:Dbtr>\n                    <Doc:Nm>JOE DOE</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>310 Field Road, NY</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Dbtr>\n                <Doc:DbtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>FOOBARC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:DbtrAgt>\n                <Doc:CdtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>BANKANC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:CdtrAgt>\n                <Doc:Cdtr>\n                    <Doc:Nm>TEST CORP</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>Nellis ABC, NV</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Cdtr>\n            </Doc:CdtTrfTxInf>\n        </Doc:FIToFICstmrCdtTrf>\n    </Doc:Document>\n</RequestPayload>";
    static String mxInvalid = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<RequestPayload>\n    <h:AppHdr xmlns:h=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">\n        <h:Fr>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>google.com</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:Fr>\n        <h:To>\n            <h:FIId>\n                <h:FinInstnId>\n                    <h:Nm>localhost</h:Nm>\n                </h:FinInstnId>\n            </h:FIId>\n        </h:To>\n        <h:BizMsgIdr>12312312312</h:BizMsgIdr>\n        <h:MsgDefIdr>pacs.008.001.09</h:MsgDefIdr>\n        <h:CreDt>2021-03-16T22:02:04.643+01:00</h:CreDt>\n    </h:AppHdr>\n    <Doc:Document xmlns:Doc=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09\">\n        <Doc:FIToFICstmrCdtTrf>\n            <Doc:GrpHdr>\n                <Doc:MsgId>TBEXO12345</Doc:MsgId>\n                <Doc:CreDtTm>2021-03-16T22:02:04.170+01:00</Doc:CreDtTm>\n                <Doc:NbOfTxs>1</Doc:NbOfTxs>\n                <Doc:SttlmInf>\n                    <Doc:SttlmMtd>INDA</Doc:SttlmMtd>\n                </Doc:SttlmInf>\n            </Doc:GrpHdr>\n            <Doc:CdtTrfTxInf>\n                <Doc:PmtId>\n                    <Doc:EndToEndId>TBEXO12345</Doc:EndToEndId>\n                    <Doc:UETR>df4309a2-0705-481c-a714-cbd999549fd1</Doc:UETR>\n                </Doc:PmtId>\n                <Doc:IntrBkSttlmAmt Ccy=\"EUR\">100</Doc:IntrBkSttlmAmt>\n                <Doc:ChrgBr>DEBT</Doc:ChrgBr>\n                <Doc:Dbtr>\n                    <Doc:Nm>JOE DOE</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>310 Field Road, NY</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Dbtr>\n                <Doc:DbtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>FOOBARC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:DbtrAgt>\n                <Doc:CdtrAgt>\n                    <Doc:FinInstnId>\n                        <Doc:BICFI>BANKANC0XXX</Doc:BICFI>\n                    </Doc:FinInstnId>\n                </Doc:CdtrAgt>\n                <Doc:Cdtr>\n                    <Doc:Nm>TEST CORP</Doc:Nm>\n                    <Doc:PstlAdr>\n                        <Doc:AdrLine>Nellis ABC, NV</Doc:AdrLine>\n                    </Doc:PstlAdr>\n                </Doc:Cdtr>\n            </Doc:CdtTrfTxInf>\n        </Doc:FIToFICstmrCdtTrf>\n    </Doc:Document>\n</RequestPayload>";

    @BeforeAll
    void setUp() throws InterruptedException {
        SpringApplicationBuilder intBuild = new SpringApplicationBuilder(Decentralizediso20022Application.class);
        internal = intBuild.profiles("internal").run();

        SpringApplicationBuilder extBuild = new SpringApplicationBuilder(Decentralizediso20022Application.class);
        external = extBuild.profiles("external").run();
    }

    @Test
    void sendPacsTest() throws IOException, InterruptedException {
        byte[] encoded = mx.getBytes("utf-8");
        URL url = new URL("http://localhost:8080/api/v1/pacs/");
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

        InputStream inputStream = conn.getInputStream();
        byte[] res = new byte[2048];
        int i = 0;
        StringBuilder response = new StringBuilder();
        while ((i = inputStream.read(res)) != -1) {
            response.append(new String(res, 0, i));
        }
        inputStream.close();

        assertTrue(response.toString().contains("The message was processed."));

    }

    @AfterAll
    void tearDown() {
        internal.close();
        external.close();
    }
}
