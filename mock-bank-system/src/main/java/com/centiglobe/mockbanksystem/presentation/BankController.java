package com.centiglobe.mockbanksystem.presentation;

import org.slf4j.LoggerFactory;

import com.centiglobe.mockbanksystem.util.CreateMx;
import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00200111;
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
 * @author Cactu5
 */
@RestController
public class BankController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankController.class);

    private static double balance = 0;

    /**
     * Handles an ISO 20022 message. Only supports Pacs.008 mesasages and will
     * increment the internal balance value with the {@link ActiveCurrencyAndAmount}
     * value(s).
     * 
     * @param encodedMx The encoded ISO 20022 message to handle
     * @return The HTTP response of the sent pacs message
     */
    @PostMapping("/handle")
    public ResponseEntity<String> handleMessage(@RequestBody String encodedMx) {
        LOGGER.info("Mock bank system handling ISO 20022 message.");

        try {
            MxPacs00200111 responseMessage;

            AbstractMX mx = AbstractMX.parse(encodedMx);
            if (mx == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad message.");

            if ("pacs.008.001.09".equals(mx.getMxId().id())){
                MxPacs00800109 pacs008 = (MxPacs00800109) mx;

                responseMessage = createResponseForPacs008(pacs008);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Message could not be handled.");
            }

            return ResponseEntity.status(HttpStatus.OK)
                                 .contentType(MediaType.APPLICATION_XML)
                                 .body(responseMessage.message());
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error.");
        }
    }

    private static MxPacs00200111 createResponseForPacs008(MxPacs00800109 pacs008){
        
        String orgnlMsgNmId = pacs008.getMxId().toString();
        String orgnlMsgId = pacs008.getFIToFICstmrCdtTrf().getGrpHdr().getMsgId();

        for (CreditTransferTransaction43 transaction : pacs008.getFIToFICstmrCdtTrf().getCdtTrfTxInf()) {
            // Ignores the currency and any other metadata
            balance += transaction.getIntrBkSttlmAmt().getValue().doubleValue();
            LOGGER.info("Received payment, new balance is " + balance + ".");
        }

        BusinessAppHdrV02 header = ((BusinessAppHdrV02) pacs008.getAppHdr());
        // Swap the from and to elements in the response
        String newTo = header.getFr().getFIId().getFinInstnId().getNm();
        String newFrom = header.getTo().getFIId().getFinInstnId().getNm();

        return CreateMx.createMxPacs00200111WithHeader(orgnlMsgId, orgnlMsgNmId, newFrom, newTo);
    }
}
