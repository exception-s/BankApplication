package com.BankApp.localbankapp.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * @author Alexander Brazhkin
 */
@ControllerAdvice
public class AllExceptionHandler extends ResponseEntityExceptionHandler {

    // todo (from 2025-07-18, 16:51): Spring supers exceptions

    @ExceptionHandler({AccountNotFoundException.class})
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}