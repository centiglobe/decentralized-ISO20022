package com.centiglobe.decentralizediso20022.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;
import com.prowidesoftware.swift.utils.Lib;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientRequestException;

/**
 * Tests for the validation service.
 * 
 * @author Cactu5
 */
@SpringBootTest
@ActiveProfiles("test")
public class ValidationServiceTest {

    @Autowired
    private ValidationService vs;

    @Test
    public void testNotTrusted() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertNotTrusted.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg);

    }

    // This test will fail as the function currently allows for bad certificate
    // pinning.
    // Might be good to make this test pass by throwing an error if public key
    // pinning is being used.
    @Disabled("Current version doesn't handle bad certifiation pinning.")
    @Test
    public void testInvalidPinning() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertPinningTest.xml"));

        String msg = "unknown"; // no errors are thrown currently, so no message

        testInvalidHeaderCustom(mx, msg);
    }

    @Test
    public void testValidHeader() throws Throwable {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("example1WithHeader.xml"));

        vs.validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr());
        vs.validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr());

    }

    @Test
    public void testExpiredHeader() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertExpired.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg);

    }

    @Test
    public void testWrongHost() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertWrongHost.xml"));

        String msg = "No subject alternative DNS name matching wrong.host.badssl.com found";

        testInvalidHeaderCustom(mx, msg);
    }

    @Test
    public void testSelfSigned() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertSelfSigned.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg);
    }

    @Test
    public void testUntrustedRoot() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertUntrustedRoot.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeaderCustom(mx, msg);
    }

    @Test
    public void testRevoked() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertRevoked.xml"));

        String msg = "Certificate has been revoked";

        testInvalidHeaderCustom(mx, msg);
    }

    private void testInvalidHeaderCustom(MxPacs00800109 mx, String msg) {

        Exception e = assertThrows(WebClientRequestException.class, () -> {
            vs.validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr());
        });

        Exception e2 = assertThrows(WebClientRequestException.class, () -> {
            vs.validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr());
        });

        System.out.println("--------------------------------cause1");
        System.out.println(e.getCause());
        System.out.println(e2.getCause());
        System.out.println("--------------------------------cause2");

        assertTrue(e.getMessage().contains(msg));
        assertTrue(e2.getMessage().contains(msg));
    }
}
