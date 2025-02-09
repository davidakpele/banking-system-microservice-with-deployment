package pesco.history_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Error {
    public static ResponseEntity<ErrorHandler> createResponse(String message, HttpStatus statusCode, String details) {
        ErrorHandler errorResponse = new ErrorHandler(message, statusCode.value(), details);
        return new ResponseEntity<>(errorResponse, statusCode);
    }
}
