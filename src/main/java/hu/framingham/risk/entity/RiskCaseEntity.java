package hu.framingham.risk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_cases")
@Getter
@Setter
@NoArgsConstructor
public class RiskCaseEntity {
    @Id
    private UUID id;

    private Integer male;
    @Column(nullable = false)
    private Integer age;
    private Integer education;
    private Integer currentSmoker;
    private Integer cigsPerDay;
    @Column(name = "bp_meds")
    private Integer bpMeds;
    private Integer prevalentStroke;
    private Integer prevalentHyp;
    private Integer diabetes;
    private Double totChol;
    @Column(name = "sys_bp")
    private Double sysBp;
    @Column(name = "dia_bp")
    private Double diaBp;
    @Column(name = "bmi")
    private Double bmi;
    private Double heartRate;
    private Double glucose;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.id == null) this.id = UUID.randomUUID();
        if (this.createdAt == null) this.createdAt = Instant.now();
    }
}
