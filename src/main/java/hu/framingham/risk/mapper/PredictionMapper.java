package hu.framingham.risk.mapper;

import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.entity.PredictionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PredictionMapper {
    @Mapping(target = "caseId", source = "riskCase.id")
    PredictionResponse toResponse(PredictionEntity source);
}
