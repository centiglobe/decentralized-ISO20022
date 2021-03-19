package com.centiglobe.decentralizediso20022.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.centiglobe.decentralizediso20022.application.ValidationService.validateHeaderFrom;
import static com.centiglobe.decentralizediso20022.application.ValidationService.validateHeaderTo;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;
import com.prowidesoftware.swift.utils.Lib;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for the validation service.
 * 
 * These tests use the trust store from google
 * https://github.com/googleapis/google-api-java-client/blob/master/google-api-client/src/main/resources/com/google/api/client/googleapis/google.jks
 * Place it in the resources folder to run the tests as intenden.
 * 
 * @author Cactu5
 */
public class ValidationServiceTest {

    private final static String trustStore1 = "google.jks";
    private final static String pwd1 = "notasecret";

    @Test
    public void testNotTrusted() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertNotTrusted.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);

    }

    // This test will fail as the function currently allows for public key pinning
    // this is somthing not supported by most browsers.
    // Might be good to make this test pass by throwing an error if public key
    // pinning is being used.
    @Disabled("Current version doesn't handle bad certifiation pinning.")
    @Test
    public void testValidPinning() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertPinningTest.xml"));

        String msg = "unknown"; // no errors are thrown currently, so no message

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);
    }

    @Test
    public void testValidHeader() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("example1WithHeader.xml"));

        validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr(), 443, trustStore1, pwd1);
        validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr(), 443, trustStore1, pwd1);

    }

    @Test
    public void testExpiredHeader() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertExpired.xml"));

        String msg = "validity check failed";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);

    }

    @Test
    public void testWrongHost() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertWrongHost.xml"));

        String msg = "No subject alternative DNS name matching wrong.host.badssl.com found";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);
    }

    @Test
    public void testSelfSigned() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertSelfSigned.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);
    }

    @Test
    public void testUntrustedRoot() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertUntrustedRoot.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);
    }

    @Test
    public void testRevoked() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertRevoked.xml"));

        String msg = "Certificate has been revoked";

        testInvalidHeaderCustom(mx, msg, trustStore1, pwd1);
    }

    private void testInvalidHeaderCustom(MxPacs00800109 mx, String msg, String tuststore, String pwd) {
        Exception e = assertThrows(SSLHandshakeException.class, () -> {
            validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr(), 443, tuststore, pwd);
        });

        Exception e2 = assertThrows(SSLHandshakeException.class, () -> {
            validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr(), 443, tuststore, pwd);
        });

        // System.out.println(e.getCause());
        // System.out.println(e2.getCause());

        assertTrue(e.getMessage().contains(msg));
        assertTrue(e2.getMessage().contains(msg));
    }
}
