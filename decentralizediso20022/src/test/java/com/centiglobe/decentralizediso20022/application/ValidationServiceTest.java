package com.centiglobe.decentralizediso20022.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.centiglobe.decentralizediso20022.application.ValidationService.validateHeaderFrom;
import static com.centiglobe.decentralizediso20022.application.ValidationService.validateHeaderTo;
import static com.centiglobe.decentralizediso20022.application.ValidationService.checkSSLCertificate;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;
import com.prowidesoftware.swift.utils.Lib;

import org.junit.jupiter.api.Test;

public class ValidationServiceTest {
    @Test
    public void testValidHeader() throws Exception {

        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("example1WithHeader.xml"));

        validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr(), null, null);
        validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr(), null, null);

    }

    @Test
    public void testExpiredHeader() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertExpired.xml"));

        String msg = "validity check failed";

        testInvalidHeader(mx, msg);

    }

    // @Test
    // public void testExpired() {

    // Exception e = assertThrows(SSLHandshakeException.class, () -> {
    // ValidationService.checkSSLCertificate("https://expired.badssl.com/", null,
    // null);
    // });

    // assertTrue(e.getMessage().contains("validity check failed"));

    // }

    @Test
    public void testWrongHost() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertWrongHost.xml"));

        String msg = "No subject alternative DNS name matching wrong.host.badssl.com found";

        testInvalidHeader(mx, msg);
    }

    @Test
    public void testSelfSigned() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertSelfSigned.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeader(mx, msg);
    }

    @Test
    public void testUntrustedRoot() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertUntrustedRoot.xml"));

        String msg = "unable to find valid certification path to requested target";

        testInvalidHeader(mx, msg);
    }

    // Need to check why no error is thrown.
    @Test
    public void testRevoked() throws IOException {
        MxPacs00800109 mx = MxPacs00800109.parse(Lib.readResource("headerCertRevoked.xml"));

        String msg = "gdfgdfgfd";

        testInvalidHeader(mx, msg);
    }

    private void testInvalidHeader(MxPacs00800109 mx, String msg) {
        Exception e = assertThrows(SSLHandshakeException.class, () -> {
            validateHeaderFrom((BusinessAppHdrV02) mx.getAppHdr(), null, null);
        });

        Exception e2 = assertThrows(SSLHandshakeException.class, () -> {
            validateHeaderTo((BusinessAppHdrV02) mx.getAppHdr(), null, null);
        });

        System.out.println(e.getCause());

        assertTrue(e.getMessage().contains(msg));
        assertTrue(e2.getMessage().contains(msg));
    }
}
