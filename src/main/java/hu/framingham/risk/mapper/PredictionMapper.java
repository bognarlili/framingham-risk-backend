package hu.framingham.risk.mapper;

import hu.framingham.risk.dto.response.JournalEntryResponse;
import hu.framingham.risk.entity.PredictionEntity;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.ml.model.PredictionResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = RiskCaseMapper.class)
public interface PredictionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "riskCase", source = "riskCase")
    @Mapping(target = "modelKey", source = "modelKey")
    @Mapping(target = "modelVersion", source = "result.modelVersion")
    @Mapping(target = "probability", source = "result.probability")
    @Mapping(target = "contributions", source = "result.contributions")
    PredictionEntity toEntity(RiskCaseEntity riskCase, String modelKey, PredictionResult result);

    /** A journal entry is identified by its case; the values come from the case. */
    @Mapping(target = "id", source = "riskCase.id")
    @Mapping(target = "model", source = "modelKey")
    @Mapping(target = "values", source = "riskCase")
    JournalEntryResponse toJournalEntry(PredictionEntity source);
}
