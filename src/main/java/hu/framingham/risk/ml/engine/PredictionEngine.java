package hu.framingham.risk.ml.engine;

import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.ml.model.RiskFeatures;

public interface PredictionEngine {
    PredictionResult predict(String modelKey, RiskFeatures features);
}
