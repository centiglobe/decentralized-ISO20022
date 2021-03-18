package com.centiglobe.decentralizediso20022.presentation.error;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@ControllerAdvice
public class ExceptionHandlers implements ErrorController {

    private static final String ERR_PATH = "/error";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);
    
    /**
     * An exception handler for {@link ResponseStatusException}s
     * 
     * @param e The {@link ResponseStatusException} that was thrown
     * @return A generic error response
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException(ResponseStatusException e) {
        LOGGER.debug(e.getMessage(), e);
        return generateResponse(e.getStatus(), e.getReason());
    }

    /**
     * An exception handler for general unhandled exceptions
     * 
     * @param e The exception that was thrown
     * @return A generic error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    /**
     * Handler method for various HTTP statuses
     * 
     * @param request The HTTP request that was sent
     * @return An error response
     */
    @RequestMapping(ERR_PATH)
    public ResponseEntity handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // If there was no status attribute, default to 404
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 404;
        LOGGER.debug("Returing status " + status + ".");

        return generateResponse(HttpStatus.valueOf(status), null);
    }

    private ResponseEntity generateResponse(HttpStatus status, String message) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element root = doc.createElement("error");

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
                rawStatus.setTextContent(message);
                root.appendChild(msg);
            }
            doc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_XML).body(writer.getBuffer().toString());
        } catch (Exception e) {
            // TODO: Send something more descriptive than an empty body
            return ResponseEntity.status(status).body("");
        }
    }

    /**
     * @return The error path
     */
    @Override
    public String getErrorPath() {
        return ERR_PATH;
    }
}
