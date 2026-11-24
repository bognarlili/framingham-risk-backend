package hu.framingham.risk.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/** The values of a journal entry, with the same names and units as the request (lab values in mmol/L). */
public record RiskValuesResponse(
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
        Double glucose
) {}
