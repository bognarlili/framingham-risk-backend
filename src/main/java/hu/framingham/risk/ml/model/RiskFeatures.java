package hu.framingham.risk.ml.model;

/** Raw input, not yet model-ready. Preprocessing must match the Python pipeline.
 * {@code totChol} and {@code glucose} are in mg/dL, as in the Framingham dataset. */
public record RiskFeatures(
        Integer male, Integer age, Integer education, Integer currentSmoker,
        Integer cigsPerDay, Integer bpMeds, Integer prevalentStroke,
        Integer prevalentHyp, Integer diabetes, Double totChol,
        Double sysBp, Double diaBp, Double bmi, Double heartRate, Double glucose
) {}
