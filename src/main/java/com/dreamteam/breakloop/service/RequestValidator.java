package com.dreamteam.breakloop.service;

import com.dreamteam.breakloop.exception.RequestValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/** Bean Validation for request bodies, reported as a 400. */
@Component
public class RequestValidator {

    private final Validator validator;

    public RequestValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object body) {

        if (body == null) {
            throw new RequestValidationException("Body is required");
        }

        String violations = validator.validate(body).stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        if (!violations.isEmpty()) {
            throw new RequestValidationException(violations);
        }
    }
}
