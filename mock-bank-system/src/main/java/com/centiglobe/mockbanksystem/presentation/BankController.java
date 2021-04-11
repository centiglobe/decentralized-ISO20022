package com.centiglobe.mockbanksystem.presentation;

import org.slf4j.LoggerFactory;

import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.MxPacs00800109;
import com.prowidesoftware.swift.model.mx.dic.CreditTransferTransaction43;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
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

    /**
     * Handles an ISO 20022 message. Only supports Pacs mesasages and will
     * increment the internal balance value with the {@link ActiveCurrencyAndAmount}
     * value(s).
     * 
     * @param encodedMx The encoded ISO 20022 message to handle
     * @return The HTTP response of the sent pacs message
     */
    @PostMapping("")
    public ResponseEntity<String> handleMessage(@RequestBody String encodedMx) {
        LOGGER.info("Mock bank system handling ISO 20022 message.");
        try {
            AbstractMX mx = AbstractMX.parse(encodedMx);
            if (mx == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad message.");

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
            return ResponseEntity.status(HttpStatus.OK).body("Success.");
        }
        catch (Exception e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error.");
        }
    }
}
