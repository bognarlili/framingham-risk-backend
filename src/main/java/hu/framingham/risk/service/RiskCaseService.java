package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.RiskCaseResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.exception.ResourceNotFoundException;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.repository.RiskCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskCaseService {
    private final RiskCaseRepository repository;
    private final RiskCaseMapper mapper;

    @Transactional
    public RiskCaseResponse create(RiskCaseRequest request) {
        RiskCaseEntity entity = mapper.toEntity(request);
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Page<RiskCaseResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public RiskCaseResponse get(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public RiskCaseEntity findEntity(UUID id) {
        return repository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Risk case not found: " + id));
    }
}
