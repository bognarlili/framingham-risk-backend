package hu.framingham.risk.service;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.RiskCaseResponse;
import hu.framingham.risk.entity.RiskCaseEntity;
import hu.framingham.risk.exception.ResourceNotFoundException;
import hu.framingham.risk.mapper.RiskCaseMapper;
import hu.framingham.risk.repository.RiskCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskCaseServiceTest {
    @Mock RiskCaseRepository repository;
    @Mock RiskCaseMapper mapper;
    @InjectMocks RiskCaseService service;

    @Test
    void createSavesAndReturnsCase() {
        RiskCaseRequest request = new RiskCaseRequest(1, 45, null, 0, 0, null,
                0, 0, 0, null, 120.0, 80.0, 25.0, 72.0, null);
        RiskCaseEntity entity = new RiskCaseEntity();
        UUID id = UUID.randomUUID();
        RiskCaseResponse response = new RiskCaseResponse(id, 1, 45, null, 0, 0, null,
                0, 0, 0, null, 120.0, 80.0, 25.0, 72.0, null, Instant.now());
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        assertThat(service.create(request).id()).isEqualTo(id);
        verify(repository).saveAndFlush(entity);
    }

    @Test
    void unknownCaseReturnsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(ResourceNotFoundException.class);
    }
}
