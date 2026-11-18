package hu.framingham.risk.controller;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.RiskCaseResponse;
import hu.framingham.risk.service.RiskCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class RiskCaseController {
    private final RiskCaseService service;

    @PostMapping
    public ResponseEntity<RiskCaseResponse> create(@Valid @RequestBody RiskCaseRequest request) {
        RiskCaseResponse saved = this.service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                                                  .path("/{id}")
                                                  .buildAndExpand(saved.id())
                                                  .toUri();
        return ResponseEntity.created(location)
                             .body(saved);
    }

    @GetMapping
    public Page<RiskCaseResponse> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return this.service.list(pageable);
    }

    @GetMapping("/{id}")
    public RiskCaseResponse get(@PathVariable UUID id) {
        return this.service.get(id);
    }
}
