package com.app.municipio.application.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ApResponse> handleCustomException(ClientException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApResponse(false, "Internal server error: " + ex.getMessage(), null));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApResponse(false, "You do not have permissions to perform this action: " + ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Error in the request body. ");
        ex.getBindingResult().getFieldErrors().forEach(error -> errorMessage.append(String.format("El campo ‘%s’ con el valor ‘%s’ es invalido. Motivo: ‘%s’", error.getField(), error.getRejectedValue(), error.getDefaultMessage())));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApResponse(false, errorMessage.toString(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApResponse(false, ex.getMessage(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation error: ");
        ex.getConstraintViolations().forEach(violation ->
                errorMessage.append(violation.getMessage()).append(" ")
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApResponse(false, errorMessage.toString().trim(), null));
    }
}
