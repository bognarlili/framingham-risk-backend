package hu.framingham.risk.ml.model;

import java.util.Map;

/** Probability is in [0, 1], NOT a percentage.
 * {@code contributions}: per input feature, how much it moved the log-odds (positive = higher risk). */
public record PredictionResult(String modelVersion, double probability, Map<String, Double> contributions) {

    /** A usable engine output: named model version, a probability in [0, 1] and finite contributions. */
    public boolean isValid() {
        return this.modelVersion != null && !this.modelVersion.isBlank()
               && Double.isFinite(this.probability) && this.probability >= 0 && this.probability <= 1
               && this.contributions != null
               && this.contributions.values().stream().allMatch(Double::isFinite);
    }
}
