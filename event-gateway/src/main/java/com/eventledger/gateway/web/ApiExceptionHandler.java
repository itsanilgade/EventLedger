package com.eventledger.gateway.web;

import com.eventledger.gateway.client.AccountServiceUnavailableException;
import com.eventledger.gateway.config.TraceFilter;
import com.eventledger.gateway.service.EventConflictException;
import com.eventledger.gateway.service.EventNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    ResponseEntity<Map<String, Object>> notFound(
            EventNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(EventConflictException.class)
    ResponseEntity<Map<String, Object>> conflict(
            EventConflictException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(AccountServiceUnavailableException.class)
    ResponseEntity<Map<String, Object>> unavailable(
            AccountServiceUnavailableException exception, HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((left, right) -> left + ", " + right)
                .orElse("Validation failed");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> malformed(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON or unsupported event type/timestamp",
                request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(
            Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<Map<String, Object>> error(
            HttpStatus status, String message, HttpServletRequest request) {
        Object requestTraceId = request.getAttribute(TraceFilter.REQUEST_ATTRIBUTE);
        String traceId = Objects.toString(requestTraceId, MDC.get("traceId"));
        if (traceId == null) {
            traceId = "";
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("traceId", traceId);
        return ResponseEntity.status(status).body(body);
    }
}