package hu.framingham.risk.dto.response;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** {@code probability} is in [0, 1]; {@code contributions} maps each input feature to its
 * contribution to the log-odds (positive = higher risk). */
public record PredictionResponse(
        UUID id,
        UUID caseId,
        String modelKey,
        String modelVersion,
        Double probability,
        Map<String, Double> contributions,
        Instant createdAt
) {}
