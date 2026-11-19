package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.AssessmentResponse;
import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.ml.engine.PredictionEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/** Saves the case and then predicts if a model is available. Deliberately NOT transactional:
 * the case is committed by its own service and stays saved whatever happens afterwards. */
@Service
@RequiredArgsConstructor
public class AssessmentService {
    private static final String MODEL_UNAVAILABLE_MESSAGE =
            "Az adatokat elmentettük. A becsléshez még nincs validált modell bekötve.";

    private final RiskCaseService riskCaseService;
    private final PredictionService predictionService;
    private final PredictionEngine predictionEngine;

    public AssessmentResponse assess(RiskCaseRequest request, String modelKey) {
        UUID caseId = this.riskCaseService.create(request).id();
        if (!this.predictionEngine.isAvailable(modelKey)) {
            return AssessmentResponse.unavailable(caseId, modelKey, MODEL_UNAVAILABLE_MESSAGE);
        }
        PredictionResponse prediction = this.predictionService.create(new PredictionRequest(caseId, modelKey));
        return AssessmentResponse.available(caseId, prediction, summary(prediction.probability()));
    }

    private static String summary(double probability) {
        NumberFormat percent = NumberFormat.getNumberInstance(Locale.forLanguageTag("hu-HU"));
        percent.setMaximumFractionDigits(1);
        return "A becsült 10 éves koszorúér-betegség kockázat " + percent.format(probability * 100) + "%.";
    }
}
