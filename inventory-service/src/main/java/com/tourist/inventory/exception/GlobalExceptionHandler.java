package com.tourist.inventory.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        String message = ex.getMessage();
        HttpStatus status;

        if (message.contains("already exists")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status)
            .body(Map.of("error", message));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && message.contains("No such product exists")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", message));
        }
        if (message != null && message.contains("Insufficient stock")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", message));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", message != null ? message : "Internal server error"));
    }
}
