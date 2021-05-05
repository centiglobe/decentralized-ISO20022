package com.centiglobe.decentralizediso20022.presentation.error;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import com.centiglobe.decentralizediso20022.util.ResponseMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;

/**
 * Handler methods for various uncaught exceptions
 * 
 * @author William Stackenäs
 */
@Controller
@ControllerAdvice
public class ExceptionHandlers implements ErrorController {

    @Value("${message.empty}")
    private String EMPTY_MSG;

    @Value("${message.generic-error}")
    private String GENERIC_ERROR;

    private static final String ERR_PATH = "/error";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);
    
    /**
     * An exception handler for {@link ResponseStatusException}s
     * 
     * @param e The {@link ResponseStatusException} that was thrown
     * @return An error response based on the HTTP status of the exception
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException e) {
        LOGGER.debug(e.getMessage(), e);
        return ResponseMessage.generateError(e.getStatus(), e.getReason());
    }

    /**
     * An exception handler for {@link HttpMessageNotReadableException}s
     * It is thrown when the POST body of the HTTP message is empty, in which
     * case the server should respond with a bad request status
     * 
     * @param e The {@link ResponseStatusException} that was thrown
     * @return A generic error response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        LOGGER.debug(e.getMessage(), e);
        return ResponseMessage.generateError(HttpStatus.BAD_REQUEST, EMPTY_MSG);
    }

    /**
     * An exception handler for general unhandled exceptions
     * 
     * @param e The exception that was thrown
     * @return A generic error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return ResponseMessage.generateError(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR);
    }

    /**
     * Handler method for various HTTP statuses
     * 
     * @param request The HTTP request that was sent
     * @return An error response
     */
    @RequestMapping(ERR_PATH)
    public ResponseEntity<String> handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // If there was no status attribute, default to 404
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 404;
        LOGGER.debug("Returing status " + status + ".");

        return ResponseMessage.generateError(HttpStatus.valueOf(status), null);
    }

    /**
     * @return The error path
     */
    @Override
    public String getErrorPath() {
        return ERR_PATH;
    }
}
