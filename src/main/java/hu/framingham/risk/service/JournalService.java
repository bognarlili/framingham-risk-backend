package hu.framingham.risk.service;

import hu.framingham.risk.dto.response.JournalEntryResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.exception.ResourceNotFoundException;
import hu.framingham.risk.mapper.PredictionMapper;
import hu.framingham.risk.ml.model.PredictionResult;
import hu.framingham.risk.repository.PredictionRepository;
import hu.framingham.risk.repository.RiskCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** A user's saved assessments. Every query is limited to the given owner. */
@Service
@RequiredArgsConstructor
public class JournalService {
    private final RiskCaseRepository riskCaseRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionMapper predictionMapper;

    /** Saves the case and its prediction together; returns the new entry's id. */
    @Transactional
    public UUID save(String ownerId, RiskCaseEntity riskCase, String modelKey, PredictionResult result) {
        riskCase.setOwnerId(ownerId);
        RiskCaseEntity saved = this.riskCaseRepository.save(riskCase);
        this.predictionRepository.save(this.predictionMapper.toEntity(saved, modelKey, result));
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> list(String ownerId, Pageable pageable) {
        return this.predictionRepository.findByRiskCaseOwnerId(ownerId, pageable)
                                        .map(this.predictionMapper::toJournalEntry);
    }

    /** Another user's entry is reported as not found, so its existence is not revealed. */
    @Transactional
    public void delete(String ownerId, UUID entryId) {
        RiskCaseEntity entry = this.riskCaseRepository.findByIdAndOwnerId(entryId, ownerId)
                                                      .orElseThrow(() -> new ResourceNotFoundException(
                                                              "Journal entry not found: " + entryId));
        this.riskCaseRepository.delete(entry); // its prediction goes with it (ON DELETE CASCADE)
    }
}
