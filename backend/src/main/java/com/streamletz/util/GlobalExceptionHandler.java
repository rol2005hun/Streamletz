package com.streamletz.util;

import com.streamletz.util.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Streamletz application.
 * 
 * <p>
 * This class provides centralized exception handling across all REST
 * controllers
 * using Spring's {@code @RestControllerAdvice}. It catches exceptions thrown by
 * controllers and converts them into appropriate HTTP responses with error
 * details.
 * </p>
 * 
 * <p>
 * Handles the following exception types:
 * </p>
 * <ul>
 * <li>{@link RuntimeException} - General runtime errors (HTTP 400)</li>
 * <li>{@link UsernameNotFoundException} - User not found errors (HTTP 404)</li>
 * <li>{@link BadCredentialsException} - Authentication failures (HTTP 401)</li>
 * <li>{@link MethodArgumentNotValidException} - Validation errors (HTTP
 * 400)</li>
 * <li>{@link Exception} - Catch-all for unexpected errors (HTTP 500)</li>
 * </ul>
 * 
 * @author Streamletz Team
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles general runtime exceptions.
     * 
     * <p>
     * Catches {@link RuntimeException} and its subclasses, returning a
     * standardized error response with HTTP 400 (Bad Request) status.
     * </p>
     * 
     * @param ex the runtime exception that was thrown
     * @return ResponseEntity containing error details with HTTP 400 status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles user not found exceptions.
     * 
     * <p>
     * Catches {@link UsernameNotFoundException} thrown when a user lookup fails,
     * returning an error response with HTTP 404 (Not Found) status.
     * </p>
     * 
     * @param ex the username not found exception
     * @return ResponseEntity containing error details with HTTP 404 status
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles authentication failures.
     * 
     * <p>
     * Catches {@link BadCredentialsException} thrown during login when credentials
     * are invalid, returning a generic error message with HTTP 401 (Unauthorized)
     * status.
     * The generic message prevents username enumeration attacks.
     * </p>
     * 
     * @param ex the bad credentials exception
     * @return ResponseEntity containing error details with HTTP 401 status
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles validation errors from request body validation.
     * 
     * <p>
     * Catches {@link MethodArgumentNotValidException} thrown when {@code @Valid}
     * annotation validation fails. Returns a map of field names to error messages
     * with HTTP 400 (Bad Request) status.
     * </p>
     * 
     * @param ex the validation exception containing all validation errors
     * @return ResponseEntity containing a map of field names to error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles all other uncaught exceptions.
     * 
     * <p>
     * This is a catch-all handler for any exceptions not handled by more specific
     * handlers. Returns HTTP 500 (Internal Server Error) with no body to avoid
     * leaking sensitive error details to clients.
     * </p>
     * 
     * @param ex the generic exception that was thrown
     * @return ResponseEntity with HTTP 500 status and empty body
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}