package com.shadowengineer.api.web;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.servlet.http.HttpServletRequest;
import com.shadowengineer.api.auth.EmailAlreadyRegisteredException;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import com.shadowengineer.api.github.GitHubSourceException;
import com.shadowengineer.api.profile.UserProfileNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleUnauthorized(InvalidCredentialsException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required for this resource.", request);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(EmailAlreadyRegisteredException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "An account already exists for this email.", request);
    }

    @ExceptionHandler(GitHubSourceException.class)
    public ResponseEntity<ApiError> handleGitHubSource(GitHubSourceException exception, HttpServletRequest request) {
        return response(exception.status(), "GITHUB_SOURCE_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserProfileNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", "The requested profile does not exist.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Check the submitted profile fields.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(Exception exception, HttpServletRequest request) {
        logger.error("Unhandled API error for {}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "The request could not be completed.", request);
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String code, String message, HttpServletRequest request) {
        ApiError error = new ApiError(
                code,
                message,
                request.getRequestURI(),
                (String) request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        return ResponseEntity.status(status).body(error);
    }
}
