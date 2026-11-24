package hu.framingham.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.UUID;

/** One-call result for the frontend: the case is always saved; the prediction part is
 * filled only when a model is available ({@code modelStatus = AVAILABLE}), otherwise
 * {@code message} explains why. Empty fields are left out of the JSON.
 * {@code probability} is in [0, 1]; {@code contributions} are log-odds per input feature. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessmentResponse(
        UUID caseId,
        UUID predictionId,
        ModelStatus modelStatus,
        String model,
        Double probability,
        String summary,
        Map<String, Double> contributions,
        String message
) {
    public enum ModelStatus { AVAILABLE, UNAVAILABLE }

    public static AssessmentResponse available(UUID caseId, PredictionResponse prediction, String summary) {
        return new AssessmentResponse(caseId, prediction.id(), ModelStatus.AVAILABLE, prediction.modelKey(),
                                      prediction.probability(), summary, prediction.contributions(), null);
    }

    public static AssessmentResponse unavailable(UUID caseId, String modelKey, String message) {
        return new AssessmentResponse(caseId, null, ModelStatus.UNAVAILABLE, modelKey,
                                      null, null, null, message);
    }
}
