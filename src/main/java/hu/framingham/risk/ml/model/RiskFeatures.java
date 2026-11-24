package hu.framingham.risk.ml.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Raw input; the ONNX models contain the preprocessing (imputation, scaling, one-hot).
 * JSON names are the dataset column names the models were trained on (e.g. sysBP, BMI).
 * {@code totChol} and {@code glucose} are in mg/dL, as in the Framingham dataset. */
public record RiskFeatures(
        Integer male, Integer age, Integer education, Integer currentSmoker,
        Integer cigsPerDay, @JsonProperty("BPMeds") Integer bpMeds, Integer prevalentStroke,
        Integer prevalentHyp, Integer diabetes, Double totChol,
        @JsonProperty("sysBP") Double sysBp, @JsonProperty("diaBP") Double diaBp,
        @JsonProperty("BMI") Double bmi, Double heartRate, Double glucose
) {}
