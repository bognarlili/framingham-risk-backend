package hu.framingham.risk.ml.model;

/** Probability is in [0, 1], NOT a percentage. */
public record PredictionResult(String modelVersion, double probability) {}
