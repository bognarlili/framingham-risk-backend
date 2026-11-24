package hu.framingham.risk.service;

import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.ml.engine.PredictionEngine;
import hu.framingham.risk.ml.model.PredictionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Runs a model on a case; saves nothing. */
@Service
@RequiredArgsConstructor
public class PredictionService {
    private final RiskCaseMapper riskCaseMapper;
    private final PredictionEngine predictionEngine;

    public PredictionResult predict(RiskCaseEntity riskCase, String modelKey) {
        PredictionResult result = this.predictionEngine.predict(modelKey, this.riskCaseMapper.toFeatures(riskCase));
        if (!result.isValid()) {
            throw new IllegalStateException("Invalid output from prediction engine");
        }
        return result;
    }
}
