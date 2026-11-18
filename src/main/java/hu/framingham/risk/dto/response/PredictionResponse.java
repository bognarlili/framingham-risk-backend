package hu.framingham.risk.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PredictionResponse(
        UUID id,
        UUID caseId,
        String modelKey,
        String modelVersion,
        Double probability,
        Instant createdAt
) {}
