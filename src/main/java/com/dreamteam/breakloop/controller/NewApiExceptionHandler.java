package com.dreamteam.breakloop.controller;

import com.dreamteam.breakloop.exception.ConflictException;
import com.dreamteam.breakloop.exception.NotFoundException;
import com.dreamteam.breakloop.exception.RequestValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Error mapping for the activity-log, suggestion-interaction and friends endpoints
 * only; the older controllers keep their existing behavior.
 */
@RestControllerAdvice(assignableTypes = {
        ActivityLogController.class,
        SuggestionInteractionController.class,
        FriendController.class
})
public class NewApiExceptionHandler {

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            RequestValidationException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // Malformed JSON and unknown enum values
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(
            HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST,
                "Invalid value for " + e.getName());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(
            ConflictException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(Map.of("error", status.getReasonPhrase(),
                        "message", message));
    }
}
