package com.example.genai.shipment.qna.app.exception;

/**
 * Global Exception class for the application.
 */
public class AppException extends RuntimeException {

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
