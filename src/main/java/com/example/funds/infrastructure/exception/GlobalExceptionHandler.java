package com.example.funds.infrastructure.exception;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import com.example.funds.domain.exception.BusinessException;
import com.example.funds.domain.exception.ActiveSubscriptionAlreadyExistsException;
import com.example.funds.domain.exception.FundInactiveException;
import com.example.funds.domain.exception.ResourceNotFoundException;
import com.example.funds.domain.exception.SubscriptionAlreadyCancelledException;
import com.example.funds.domain.exception.UnauthorizedAccessException;
import com.example.funds.infrastructure.entrypoints.rest.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getAllValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.toString())
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                details,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                List.of(exception.getParameterName() + ": parameter is required"),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                List.of(exception.getName() + ": invalid value"),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingClientContextException.class)
    public ResponseEntity<ErrorResponse> handleMissingClientContextException(
            MissingClientContextException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication is required",
                List.of(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "Invalid email or password",
                List.of(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn("Business exception at path={}. code={}, message={}", request.getRequestURI(), exception.code(), exception.getMessage());
        return buildResponse(
                resolveHttpStatus(exception),
                exception.code(),
                exception.getMessage(),
                List.of(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn("Access denied at path={}", request.getRequestURI());
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Access denied",
                List.of(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Unhandled exception at path={}", request.getRequestURI(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                List.of(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            List<String> details,
            String path
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        code,
                        message,
                        details,
                        path,
                        OffsetDateTime.now()
                )
        );
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private HttpStatus resolveHttpStatus(BusinessException exception) {
        if (exception instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof UnauthorizedAccessException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exception instanceof SubscriptionAlreadyCancelledException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof ActiveSubscriptionAlreadyExistsException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof FundInactiveException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
