package hu.framingham.risk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PredictionRequest(
        @NotNull UUID caseId,
        @NotBlank @Size(max = 64) String modelKey
) {}
