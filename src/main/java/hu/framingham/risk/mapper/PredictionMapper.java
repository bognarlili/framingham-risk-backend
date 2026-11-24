package hu.framingham.risk.mapper;

import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.entity.PredictionEntity;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.ml.model.PredictionResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PredictionMapper {
    @Mapping(target = "caseId", source = "riskCase.id")
    PredictionResponse toResponse(PredictionEntity source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "riskCase", source = "riskCase")
    @Mapping(target = "modelKey", source = "modelKey")
    @Mapping(target = "modelVersion", source = "result.modelVersion")
    @Mapping(target = "probability", source = "result.probability")
    @Mapping(target = "contributions", source = "result.contributions")
    PredictionEntity toEntity(RiskCaseEntity riskCase, String modelKey, PredictionResult result);
}
