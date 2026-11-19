package hu.framingham.risk.dto.response;

import java.util.Map;
import java.util.UUID;

/** One-call result for the frontend: the case is always saved; the prediction part is
 * filled only when a validated model is available ({@code modelStatus = AVAILABLE}).
 * {@code probability} is in [0, 1]; it is null (never invented) when the model is unavailable. */
public record AssessmentResponse(
        UUID caseId,
        UUID predictionId,
        ModelStatus modelStatus,
        String model,
        Double probability,
        String summary,
        Map<String, Double> contributions,
        Map<String, String> interpretations,
        String message
) {
    public enum ModelStatus { AVAILABLE, UNAVAILABLE }

    public static AssessmentResponse available(UUID caseId, PredictionResponse prediction, String summary) {
        return new AssessmentResponse(caseId, prediction.id(), ModelStatus.AVAILABLE, prediction.modelKey(),
                                      prediction.probability(), summary, Map.of(), null, null);
    }

    public static AssessmentResponse unavailable(UUID caseId, String modelKey, String message) {
        return new AssessmentResponse(caseId, null, ModelStatus.UNAVAILABLE, modelKey,
                                      null, null, Map.of(), null, message);
    }
}
