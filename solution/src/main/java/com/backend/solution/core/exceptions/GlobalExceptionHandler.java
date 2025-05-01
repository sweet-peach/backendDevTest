package com.backend.solution.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorDetails> buildResponseEntity(HttpStatus status, Exception exception) {
        ErrorDetails errorObject = new ErrorDetails();
        errorObject.setCode(status.value());
        errorObject.setMessage(exception.getMessage());

        return new ResponseEntity<>(errorObject, status);
    }

    private ResponseEntity<ErrorDetails> buildResponseEntity(HttpStatus status, String message) {
        ErrorDetails errorObject = new ErrorDetails();
        errorObject.setCode(status.value());
        errorObject.setMessage(message);

        return new ResponseEntity<>(errorObject, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleAll(Exception ex, WebRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }
}
