package hu.framingham.risk.repository;

import hu.framingham.risk.entity.PredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PredictionRepository extends JpaRepository<PredictionEntity, UUID> {}
