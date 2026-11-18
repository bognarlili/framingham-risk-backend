package hu.framingham.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record RiskCaseResponse(
        UUID id,
        Integer male,
        Integer age,
        Integer education,
        Integer currentSmoker,
        Integer cigsPerDay,
        @JsonProperty("BPMeds") Integer bpMeds,
        Integer prevalentStroke,
        Integer prevalentHyp,
        Integer diabetes,
        Double totChol,
        @JsonProperty("sysBP") Double sysBp,
        @JsonProperty("diaBP") Double diaBp,
        @JsonProperty("BMI") Double bmi,
        Double heartRate,
        Double glucose,
        Instant createdAt
) {}
