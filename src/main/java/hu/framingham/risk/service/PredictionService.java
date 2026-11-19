package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.entity.PredictionEntity;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.mapper.PredictionMapper;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.ml.engine.PredictionEngine;
import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PredictionService {
    private final PredictionRepository predictionRepository;
    private final RiskCaseService riskCaseService;
    private final RiskCaseMapper riskCaseMapper;
    private final PredictionMapper predictionMapper;
    private final PredictionEngine predictionEngine;

    @Transactional
    public PredictionResponse create(PredictionRequest request) {
        RiskCaseEntity riskCase = this.riskCaseService.findEntity(request.caseId());
        PredictionResult result = this.predictionEngine.predict(request.modelKey(),
                                                                this.riskCaseMapper.toFeatures(riskCase));
        if (!result.isValid()) {
            throw new IllegalStateException("Invalid output from prediction engine");
        }
        PredictionEntity entity = this.predictionMapper.toEntity(riskCase, request.modelKey(), result);
        return this.predictionMapper.toResponse(this.predictionRepository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Page<PredictionResponse> list(Pageable pageable) {
        return this.predictionRepository.findAll(pageable)
                                        .map(this.predictionMapper::toResponse);
    }
}
