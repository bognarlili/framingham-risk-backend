package hu.framingham.risk.repository;

import hu.framingham.risk.entity.RiskCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RiskCaseRepository extends JpaRepository<RiskCaseEntity, UUID> {}
