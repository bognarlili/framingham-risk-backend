package hu.framingham.risk.repository;

import hu.framingham.risk.entity.PredictionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PredictionRepository extends JpaRepository<PredictionEntity, UUID> {
    /** Loads the case in the same query (no extra query per entry). */
    @EntityGraph(attributePaths = "riskCase")
    Page<PredictionEntity> findByRiskCaseOwnerId(String ownerId, Pageable pageable);
}
