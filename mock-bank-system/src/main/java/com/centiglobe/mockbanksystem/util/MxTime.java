package com.centiglobe.mockbanksystem.util;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Util class for creating a string for time compatible with ISO 20022 messages.
 * 
 * @author Cactu5
 */
public class MxTime {
    /**
     * Returns the current time in <code>XMLGregorianCalendar</code> format.
     * 
     * Note: The function is taken from:
     * https://github.com/prowide/prowide-iso20022-examples/blob/main/src/main/java/com/prowidesoftware/swift/samples/MxCreation2Example.java
     * 
     * @return the current time
     */
    public static XMLGregorianCalendar getNow(){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = null;
        
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        
        return now;
    }
}
