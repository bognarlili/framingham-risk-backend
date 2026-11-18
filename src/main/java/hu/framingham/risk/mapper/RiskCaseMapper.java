package hu.framingham.risk.mapper;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.RiskCaseResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.ml.model.RiskFeatures;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RiskCaseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RiskCaseEntity toEntity(RiskCaseRequest source);

    RiskCaseResponse toResponse(RiskCaseEntity source);

    RiskFeatures toFeatures(RiskCaseEntity source);
}
