package hu.framingham.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import hu.framingham.risk.ml.model.PredictionResult;

import java.util.Map;
import java.util.UUID;

/** Result of one assessment. Logged-in users get it saved to their journal ({@code saved},
 * {@code entryId}); guests only get the result. If the model is unavailable, {@code message}
 * explains why. Empty fields are left out of the JSON.
 * {@code probability} is in [0, 1]; {@code contributions} are log-odds per input feature. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessmentResponse(
        UUID entryId,
        boolean saved,
        ModelStatus modelStatus,
        String model,
        Double probability,
        String summary,
        Map<String, Double> contributions,
        String message
) {
    public enum ModelStatus { AVAILABLE, UNAVAILABLE }

    /** @param entryId the journal entry, or null for a guest (nothing saved) */
    public static AssessmentResponse available(UUID entryId, String modelKey, PredictionResult result,
                                               String summary) {
        return new AssessmentResponse(entryId, entryId != null, ModelStatus.AVAILABLE, modelKey,
                                      result.probability(), summary, result.contributions(), null);
    }

    public static AssessmentResponse unavailable(String modelKey, String message) {
        return new AssessmentResponse(null, false, ModelStatus.UNAVAILABLE, modelKey, null, null, null, message);
    }
}
