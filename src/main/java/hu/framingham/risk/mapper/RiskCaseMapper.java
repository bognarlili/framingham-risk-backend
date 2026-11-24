package hu.framingham.risk.mapper;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.RiskValuesResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.ml.model.LabUnits;
import hu.framingham.risk.ml.model.RiskFeatures;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RiskCaseMapper {
    /** Not yet saved and without owner: guests' cases are never saved. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RiskCaseEntity toEntity(RiskCaseRequest source);

    RiskValuesResponse toValues(RiskCaseEntity source);

    /** The entity holds lab values in mmol/L; the model expects the dataset's mg/dL. */
    @Mapping(target = "totChol", source = "totChol", qualifiedByName = "cholesterolToMgDl")
    @Mapping(target = "glucose", source = "glucose", qualifiedByName = "glucoseToMgDl")
    RiskFeatures toFeatures(RiskCaseEntity source);

    @Named("cholesterolToMgDl")
    default Double cholesterolToMgDl(Double mmolPerLiter) {
        return LabUnits.cholesterolToMgDl(mmolPerLiter);
    }

    @Named("glucoseToMgDl")
    default Double glucoseToMgDl(Double mmolPerLiter) {
        return LabUnits.glucoseToMgDl(mmolPerLiter);
    }
}
