package hu.framingham.risk.exception;

import hu.framingham.risk.dto.response.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception) {
        return this.error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ModelUnavailableException.class)
    ResponseEntity<ApiError> modelUnavailable(ModelUnavailableException exception) {
        return this.error(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalidRequest(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new TreeMap<>();
        exception.getBindingResult()
                 .getFieldErrors()
                 .forEach(field ->
                                  fields.putIfAbsent(field.getField(), field.getDefaultMessage()));
        return this.error(HttpStatus.BAD_REQUEST, "Request validation failed", fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> invalidPayload(Exception exception) {
        return this.error(HttpStatus.BAD_REQUEST, "Invalid request payload", Map.of());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> fields) {
        return ResponseEntity.status(status)
                             .body(ApiError.of(status, message, fields));
    }
}
