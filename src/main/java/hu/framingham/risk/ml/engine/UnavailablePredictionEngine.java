package hu.framingham.risk.ml.engine;

import hu.framingham.risk.exception.ModelUnavailableException;
import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.ml.model.RiskFeatures;
import org.springframework.stereotype.Component;

/** Real inference will replace this adapter when the validated model is ready. */
@Component
public class UnavailablePredictionEngine implements PredictionEngine {
    @Override
    public PredictionResult predict(String modelKey, RiskFeatures features) {
        throw new ModelUnavailableException(
                "No validated model is configured. Export and verify the Python preprocessing pipeline first.");
    }
}
