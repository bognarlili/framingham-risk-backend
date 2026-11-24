package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.AssessmentResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.ml.engine.PredictionEngine;
import hu.framingham.risk.ml.model.PredictionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

/** Estimates the risk; for a logged-in user the case and result also go into their journal. */
@Service
@RequiredArgsConstructor
public class AssessmentService {
    private static final String MODEL_UNAVAILABLE_MESSAGE = "A kiválasztott modell nem érhető el.";

    private final RiskCaseMapper riskCaseMapper;
    private final PredictionService predictionService;
    private final JournalService journalService;
    private final PredictionEngine predictionEngine;

    /** @param ownerId the logged-in user's id, or null for a guest (nothing is saved) */
    public AssessmentResponse assess(RiskCaseRequest request, String modelKey, String ownerId) {
        if (!this.predictionEngine.isAvailable(modelKey)) {
            return AssessmentResponse.unavailable(modelKey, MODEL_UNAVAILABLE_MESSAGE);
        }
        RiskCaseEntity riskCase = this.riskCaseMapper.toEntity(request);
        PredictionResult result = this.predictionService.predict(riskCase, modelKey);
        UUID entryId = ownerId == null ? null : this.journalService.save(ownerId, riskCase, modelKey, result);
        return AssessmentResponse.available(entryId, modelKey, result, summary(result.probability()));
    }

    private static String summary(double probability) {
        NumberFormat percent = NumberFormat.getNumberInstance(Locale.forLanguageTag("hu-HU"));
        percent.setMaximumFractionDigits(1);
        return "A becsült 10 éves koszorúér-betegség kockázat " + percent.format(probability * 100) + "%.";
    }
}
