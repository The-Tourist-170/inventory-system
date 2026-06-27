package com.tourist.order.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = extractStatus(message);

        return ResponseEntity.status(status)
                .body(Map.of(
                        "status", status.value(),
                        "error", extractErrorMessage(message)));
    }

    private HttpStatus extractStatus(String message) {
        if (message == null) return HttpStatus.INTERNAL_SERVER_ERROR;
        if (message.contains(" 404 ")) return HttpStatus.NOT_FOUND;
        if (message.contains(" 409 ")) return HttpStatus.CONFLICT;
        if (message.contains(" 400 ")) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String extractErrorMessage(String message) {
        if (message == null) return "Internal server error";

        int jsonStart = message.indexOf("{\"error\":\"");
        if (jsonStart >= 0) {
            int start = jsonStart + 10;
            int end = message.indexOf("\"", start);
            if (end >= 0) {
                return message.substring(start, end);
            }
        }

        if (message.startsWith("Cannot place order")) {
            int colon = message.lastIndexOf(':');
            return colon >= 0 ? message.substring(colon + 1).trim() : "Order could not be placed";
        }

        return message;
    }
}
