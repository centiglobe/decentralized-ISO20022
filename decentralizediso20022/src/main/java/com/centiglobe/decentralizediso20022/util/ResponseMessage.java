package com.centiglobe.decentralizediso20022.util;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A utility class for generating response messages
 * 
 * @author William Stackenäs
 */
public class ResponseMessage {
    
    /**
     * Generates an error HTTP response message with the given status
     * 
     * @param status The HTTP status of the response
     * @param message A detailed message explaining why the error happened
     * @return An error HTTP response
     */
    public static ResponseEntity<String> generateError(HttpStatus status, String message) {
        return generate("error", status, message);
    }

    /**
     * Generates a 200 OK HTTP response message indicating success
     * 
     * @param message A detailed message
     * @return A HTTP response indicating success
     */
    public static ResponseEntity<String> generateSuccess(String message) {
        return generate("success", HttpStatus.OK, message);
    }

    private static ResponseEntity<String> generate(String prefix, HttpStatus status, String message) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element root = doc.createElement(prefix);

            Element timestamp = doc.createElement("timestamp");
            timestamp.setTextContent(
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now())
            );
            root.appendChild(timestamp);

            Element rawStatus = doc.createElement("status");
            rawStatus.setTextContent(String.format("%d", status.value()));
            root.appendChild(rawStatus);

            Element code = doc.createElement("code");
            code.setTextContent(status.name().replace("_", " "));
            root.appendChild(code);

            if (message != null) {
                Element msg = doc.createElement("message");
                msg.setTextContent(message);
                root.appendChild(msg);
            }
            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_XML).body(writer.getBuffer().toString());
        } catch (Exception e) {
            // TODO: Potentially send something more descriptive than an empty body
            return ResponseEntity.status(status).body("");
        }
    }
}
