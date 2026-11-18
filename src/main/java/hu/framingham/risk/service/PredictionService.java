package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.entity.PredictionEntity;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.exception.ResourceNotFoundException;
import hu.framingham.risk.mapper.PredictionMapper;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.ml.engine.PredictionEngine;
import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.repository.PredictionRepository;
import hu.framingham.risk.repository.RiskCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final PredictionRepository predictionRepository;
    private final RiskCaseRepository riskCaseRepository;
    private final RiskCaseMapper riskCaseMapper;
    private final PredictionMapper predictionMapper;
    private final PredictionEngine predictionEngine;

    @Transactional
    public PredictionResponse create(PredictionRequest request) {
        RiskCaseEntity riskCase = this.riskCaseRepository.findById(request.caseId())
                                                         .orElseThrow(() -> new ResourceNotFoundException(
                                                            "Risk case not found: " + request.caseId()));
        PredictionResult result = this.predictionEngine.predict(request.modelKey(),
                                                                this.riskCaseMapper.toFeatures(riskCase));
        if (result.modelVersion() == null || result.modelVersion()
                                                   .isBlank() ||
            !Double.isFinite(result.probability()) || result.probability() < 0 || result.probability() > 1) {
            throw new IllegalStateException("Invalid output from prediction engine");
        }
        PredictionEntity entity = new PredictionEntity();
        entity.setRiskCase(riskCase);
        entity.setModelKey(request.modelKey());
        entity.setModelVersion(result.modelVersion());
        entity.setProbability(result.probability());
        return this.predictionMapper.toResponse(this.predictionRepository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Page<PredictionResponse> list(Pageable pageable) {
        return this.predictionRepository.findAll(pageable)
                                        .map(this.predictionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PredictionResponse get(UUID id) {
        return this.predictionRepository.findById(id)
                                        .map(this.predictionMapper::toResponse)
                                        .orElseThrow(() -> new ResourceNotFoundException("Prediction not found: " + id));
    }
}
