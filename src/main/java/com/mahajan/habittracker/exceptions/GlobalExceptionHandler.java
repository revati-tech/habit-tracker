package com.mahajan.habittracker.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHabitNotFoundException(HabitNotFoundException e, HttpServletRequest request) {
        log.error("Habit not found: {}", e.getMessage(), e); // logs stack
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.error("User not found: {}", e.getMessage(), e); // logs stack
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    // ------------------ 400 Bad Request ------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("Validation failed", e);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,  // missing/invalid body
            BindException.class,                    // form binding errors
            MissingServletRequestParameterException.class
    })
    
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e, HttpServletRequest request) {
        log.error("Bad Request", e);
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(HabitAlreadyCompletedException.class)
    public ResponseEntity<ErrorResponse> handleHabitAlreadyCompleted(HabitAlreadyCompletedException e, HttpServletRequest request) {
        log.warn("Habit already completed: {}", e.getMessage());
        return buildResponse(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(HabitCompletionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHabitCompletionNotFound(HabitCompletionNotFoundException e, HttpServletRequest request) {
        log.warn("Habit completion not found: {}", e.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }


    @ExceptionHandler(Exception.class) // fallback for anything else
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e, HttpServletRequest request) {
        log.error("Unexpected error occurred", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), request);
    }

    public ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message).path(request.getRequestURI()).timestamp(Instant.now()).build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}