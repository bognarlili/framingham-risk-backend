package hu.framingham.risk.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

/**
 * Input uses the 15 feature names from the Framingham research. Nullable fields
 * are retained as missing; no imputation or categorical recoding happens here.
 * <p>Units: {@code totChol} and {@code glucose} are in <b>mmol/L</b>. They are stored
 * as received and converted to the dataset's mg/dL only when building model features.
 * The upper bounds reject values that were obviously entered in mg/dL.
 */
public record RiskCaseRequest(
        @Min(0) @Max(1) Integer male,
        @NotNull @Min(0) @Max(120) Integer age,
        @Min(1) @Max(4) Integer education,
        @Min(0) @Max(1) Integer currentSmoker,
        @PositiveOrZero Integer cigsPerDay,
        @Min(0) @Max(1) @JsonProperty("BPMeds") Integer bpMeds,
        @Min(0) @Max(1) Integer prevalentStroke,
        @Min(0) @Max(1) Integer prevalentHyp,
        @Min(0) @Max(1) Integer diabetes,
        @Positive @DecimalMax(value = "20.0", message = "must be in mmol/L (at most 20)") Double totChol,
        @Positive @JsonProperty("sysBP") Double sysBp,
        @Positive @JsonProperty("diaBP") Double diaBp,
        @Positive @JsonProperty("BMI") Double bmi,
        @Positive Double heartRate,
        @Positive @DecimalMax(value = "40.0", message = "must be in mmol/L (at most 40)") Double glucose
) {}
