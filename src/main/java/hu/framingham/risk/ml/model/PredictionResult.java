package hu.framingham.risk.ml.model;

/** Probability is in [0, 1], NOT a percentage. */
public record PredictionResult(String modelVersion, double probability) {

    /** A usable engine output: named model version and a finite probability in [0, 1]. */
    public boolean isValid() {
        return this.modelVersion != null && !this.modelVersion.isBlank()
               && Double.isFinite(this.probability) && this.probability >= 0 && this.probability <= 1;
    }
}
