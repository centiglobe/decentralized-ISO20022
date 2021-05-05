package com.centiglobe.mockbanksystem.util;

import java.util.Random;

import com.prowidesoftware.swift.model.mx.BusinessAppHdrV02;
import com.prowidesoftware.swift.model.mx.MxPacs00200111;
import com.prowidesoftware.swift.model.mx.dic.BranchAndFinancialInstitutionIdentification6;
import com.prowidesoftware.swift.model.mx.dic.FIToFIPaymentStatusReportV11;
import com.prowidesoftware.swift.model.mx.dic.FinancialInstitutionIdentification18;
import com.prowidesoftware.swift.model.mx.dic.GroupHeader91;
import com.prowidesoftware.swift.model.mx.dic.OriginalGroupHeader17;
import com.prowidesoftware.swift.model.mx.dic.Party44Choice;

/**
 * A utility class for help in creating ISO 20022 messages.
 * 
 * @author Cactu5
 */
public class CreateMx {
    
    private static Random rand = new Random();

    /**
     * Creates an appropriate pacs.002.001.11 message with a header based of <code>orgnlMsgId</code>,
     * <code>orgnlMsgNmId</code>, <code>from</code> and <code>to</code>.
     * 
     * @param orgnlMsgId the message id of the message the status is for.
     * @param orgnlMsgNmId the full message name of the message the status is for.
     * @param from the destination domain
     * @param to the source domain
     * @return the pacs.002.001.11 message
     */
    public static MxPacs00200111 createMxPacs00200111WithHeader(String orgnlMsgId, String orgnlMsgNmId, String from, String to){
        MxPacs00200111 mx = new MxPacs00200111();

        // initialize group header
        mx.setFIToFIPmtStsRpt(new FIToFIPaymentStatusReportV11().setGrpHdr(new GroupHeader91()));

        // general information
        mx.getFIToFIPmtStsRpt().getGrpHdr().setMsgId("ABC"+rand.nextInt(100000));
        mx.getFIToFIPmtStsRpt().getGrpHdr().setCreDtTm(MxTime.getNow());

        // add what message this is the status report for
        OriginalGroupHeader17 orgHeader = new OriginalGroupHeader17();
        orgHeader.setOrgnlMsgId(orgnlMsgId);
        orgHeader.setOrgnlMsgNmId(orgnlMsgNmId);

        mx.getFIToFIPmtStsRpt().addOrgnlGrpInfAndSts(orgHeader);

        BusinessAppHdrV02 header = new BusinessAppHdrV02();

        // from and to elements
        header.setFr((new Party44Choice()).setFIId((new BranchAndFinancialInstitutionIdentification6())
                .setFinInstnId((new FinancialInstitutionIdentification18()).setNm(from))));
        header.setTo((new Party44Choice()).setFIId((new BranchAndFinancialInstitutionIdentification6())
                .setFinInstnId((new FinancialInstitutionIdentification18()).setNm(to))));

        header.setBizMsgIdr("ABC" + rand.nextInt(100000));

        // message definition identifier for
        // the message instance that is being transported
        header.setMsgDefIdr(mx.getMxId().toString());
        
        header.setCreDt(MxTime.getNow());
        mx.setAppHdr(header);
        
        return mx;
    }
}
