package com.auction.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex, HttpServletRequest req) {
        return body(ex.getStatus(), ex.getCode(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        Map<String, String> fields = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors())
            fields.put(fe.getField(), fe.getDefaultMessage());
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed", req, fields);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex,
                                                                HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex,
                                                                  HttpServletRequest req) {
        return body(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req, null);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex,
                                                                      HttpServletRequest req) {
        return body(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Something went wrong", req, null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String msg,
                                                     HttpServletRequest req,
                                                     Map<String, String> fields) {
        Map<String, Object> m = new HashMap<>();
        m.put("timestamp", LocalDateTime.now().toString());
        m.put("status", status.value());
        m.put("code", code);
        m.put("message", msg);
        m.put("path", req.getRequestURI());
        if (fields != null) m.put("fieldErrors", fields);
        return ResponseEntity.status(status).body(m);
    }
}