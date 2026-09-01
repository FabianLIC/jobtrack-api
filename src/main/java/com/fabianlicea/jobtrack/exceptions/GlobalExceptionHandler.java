package com.fabianlicea.jobtrack.exceptions;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fabianlicea.jobtrack.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ApplicationNotFoundException.class)
        public ResponseEntity<ApiError> handleApplicationNotFound(ApplicationNotFoundException ex,
                        HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                                                HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(),
                                                request.getRequestURI(), null));
        }

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                                                HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(),
                                                request.getRequestURI(), null));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleArgumentNotValid(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                Map<String, String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .collect(Collectors.toMap(
                                                error -> error.getField(),
                                                error -> error.getDefaultMessage(),
                                                (mensaje1, mensaje2) -> mensaje1));

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed",
                                                request.getRequestURI(),
                                                errors));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                        HttpServletRequest request) {
                log.warn("Malformed JSON request processing request to {}: ", request.getRequestURI(), ex);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                                                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Malformed JSON request",
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiError> handleGlobalException(Exception ex,
                        HttpServletRequest request) {

                log.error("An unexpected error occurred processing request to {}: ", request.getRequestURI(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                                "An unexpected error occurred",
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleEmailAlreadyExists(Exception ex,
                        HttpServletRequest request) {

                log.error("Your request clashes with the current state of the resource request to {}: ",
                                request.getRequestURI(), ex);

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                                                HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(),
                                                request.getRequestURI(),
                                                null));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiError> handleBadCredentials(Exception ex,
                        HttpServletRequest request) {

                log.warn("Invalid credentials request to {}: ", request.getRequestURI(), ex);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(new ApiError(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                                                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                                "Invalid credentials",
                                                request.getRequestURI(),
                                                null));
        }

}
