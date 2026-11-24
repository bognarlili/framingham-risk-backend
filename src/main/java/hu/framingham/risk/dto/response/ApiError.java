package hu.framingham.risk.dto.response;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String error, String message,
                       Map<String, String> details) {

    public static ApiError of(HttpStatus status, String message, Map<String, String> details) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, details);
    }
}
