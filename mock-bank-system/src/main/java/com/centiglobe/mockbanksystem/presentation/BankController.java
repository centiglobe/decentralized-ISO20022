package com.centiglobe.mockbanksystem.presentation;

import org.slf4j.LoggerFactory;

import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransaction43;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller for handling ISO 20022 message
 * from the external component
 * 
 * @author William Stackenäs
 */
@RestController
public class BankController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankController.class);

    private static double balance = 0;

    private static String pacs002 =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
    "<RequestPayload>" +
    "    <h:AppHdr xmlns:h=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\">" +
    "        <h:Fr>" +
    "            <h:FIId>" +
    "                <h:FinInstnId>" +
    "                    <h:Nm>%s</h:Nm>" +
    "                </h:FinInstnId>" +
    "            </h:FIId>" +
    "        </h:Fr>" +
    "        <h:To>" +
    "            <h:FIId>" +
    "                <h:FinInstnId>" +
    "                    <h:Nm>%s</h:Nm>" +
    "                </h:FinInstnId>" +
    "            </h:FIId>" +
    "        </h:To>" +
    "        <h:BizMsgIdr>ABC1456456</h:BizMsgIdr>" +
    "        <h:MsgDefIdr>pacs.002.001.11</h:MsgDefIdr>" +
    "        <h:CreDt>2021-03-19T10:20:20.465+01:00</h:CreDt>" +
    "    </h:AppHdr>" +
    "    <Doc:Document xmlns:Doc=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.11\">" +
    "        <Doc:FIToFIPmtStsRpt>" +
    "            <Doc:GrpHdr>" +
    "                <Doc:MsgId>ABC12345</Doc:MsgId>" +
    "                <Doc:CreDtTm>2021-03-19T10:20:19.933+01:00</Doc:CreDtTm>" +
    "            </Doc:GrpHdr>" +
    "            <Doc:OrgnlGrpInfAndSts>" +
    "                <Doc:OrgnlMsgId>TBEXO12345</Doc:OrgnlMsgId>" +
    "                <Doc:OrgnlMsgNmId>pacs.008.001.09</Doc:OrgnlMsgNmId>" +
    "            </Doc:OrgnlGrpInfAndSts>" +
    "        </Doc:FIToFIPmtStsRpt>" +
    "    </Doc:Document>" +
    "</RequestPayload>";

    /**
     * Handles an ISO 20022 message. Only supports Pacs mesasages and will
     * increment the internal balance value with the {@link ActiveCurrencyAndAmount}
     * value(s).
     * 
     * @param encodedMx The encoded ISO 20022 message to handle
     * @return The HTTP response of the sent pacs message
     */
    @PostMapping("/handle")
    public ResponseEntity<String> handleMessage(@RequestBody String encodedMx) {
        LOGGER.info("Mock bank system handling ISO 20022 message.");
        String from;
        String to;
        try {
            AbstractMX mx = AbstractMX.parse(encodedMx);
            if (mx == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad message.");

            BusinessAppHdrV02 header = ((BusinessAppHdrV02) mx.getAppHdr());
            if ("pacs".equals(mx.getBusinessProcess())) {
                MxPacs00800109 pacs = (MxPacs00800109) mx;
                for (CreditTransferTransaction43 transaction : pacs.getFIToFICstmrCdtTrf().getCdtTrfTxInf()) {
                    // Ignores the currency and any other metadata
                    balance += transaction.getIntrBkSttlmAmt().getValue().doubleValue();
                    LOGGER.info("Received payment, new balance is " + balance + ".");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Message could not be handled.");
            }
            // Swap the from and to fields in the response
            from = header.getFr().getFIId().getFinInstnId().getNm();
            to = header.getTo().getFIId().getFinInstnId().getNm();
            return ResponseEntity.status(HttpStatus.OK)
                                 .contentType(MediaType.APPLICATION_XML)
                                 .body(String.format(pacs002, to, from));
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error.");
        }
    }
}
