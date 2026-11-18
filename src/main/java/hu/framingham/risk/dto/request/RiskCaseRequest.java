package hu.framingham.risk.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/** Input uses the 15 feature names from the Framingham research. Nullable fields
 * are retained as missing; no imputation or categorical recoding happens here. */
public record RiskCaseRequest(
        @Min(0) @Max(1) Integer male,
        @NotNull @Min(0) @Max(120) Integer age,
        @Positive Integer education,
        @Min(0) @Max(1) Integer currentSmoker,
        @PositiveOrZero Integer cigsPerDay,
        @Min(0) @Max(1) @JsonProperty("BPMeds") Integer bpMeds,
        @Min(0) @Max(1) Integer prevalentStroke,
        @Min(0) @Max(1) Integer prevalentHyp,
        @Min(0) @Max(1) Integer diabetes,
        @Positive Double totChol,
        @Positive @JsonProperty("sysBP") Double sysBp,
        @Positive @JsonProperty("diaBP") Double diaBp,
        @Positive @JsonProperty("BMI") Double bmi,
        @Positive Double heartRate,
        @Positive Double glucose
) {}
