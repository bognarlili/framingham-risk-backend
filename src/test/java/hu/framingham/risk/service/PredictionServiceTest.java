package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.exception.ModelUnavailableException;
import hu.framingham.risk.mapper.PredictionMapper;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.ml.engine.PredictionEngine;
import hu.framingham.risk.ml.model.RiskFeatures;
import hu.framingham.risk.repository.PredictionRepository;
import hu.framingham.risk.repository.RiskCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {
    @Mock PredictionRepository predictions;
    @Mock RiskCaseRepository cases;
    @Mock RiskCaseMapper caseMapper;
    @Mock PredictionMapper predictionMapper;
    @Mock PredictionEngine engine;
    @InjectMocks PredictionService service;

    @Test
    void unavailableModelDoesNotSaveFabricatedPrediction() {
        UUID id = UUID.randomUUID();
        RiskCaseEntity entity = new RiskCaseEntity();
        RiskFeatures features = new RiskFeatures(null, 45, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        when(cases.findById(id)).thenReturn(Optional.of(entity));
        when(caseMapper.toFeatures(entity)).thenReturn(features);
        when(engine.predict("logistic-regression-full", features))
                .thenThrow(new ModelUnavailableException("No model"));

        assertThatThrownBy(() -> service.create(new PredictionRequest(id, "logistic-regression-full")))
                .isInstanceOf(ModelUnavailableException.class);
        verifyNoInteractions(predictions);
    }
}
