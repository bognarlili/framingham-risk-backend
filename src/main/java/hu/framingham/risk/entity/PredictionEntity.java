package hu.framingham.risk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
public class PredictionEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "risk_case_id", nullable = false)
    private RiskCaseEntity riskCase;

    @Column(nullable = false, length = 64)
    private String modelKey;
    @Column(nullable = false, length = 64)
    private String modelVersion;
    @Column(nullable = false)
    private Double probability;
    /** Feature name -> contribution to the log-odds (positive = higher risk). */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Double> contributions;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
