package hu.framingham.risk.controller;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.service.PredictionService;
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
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {
    private final PredictionService service;

    @PostMapping
    public ResponseEntity<PredictionResponse> create(@Valid @RequestBody PredictionRequest request) {
        PredictionResponse saved = this.service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                                                  .path("/{id}")
                                                  .buildAndExpand(saved.id())
                                                  .toUri();
        return ResponseEntity.created(location)
                             .body(saved);
    }

    @GetMapping
    public Page<PredictionResponse> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return this.service.list(pageable);
    }

    @GetMapping("/{id}")
    public PredictionResponse get(@PathVariable UUID id) {
        return this.service.get(id);
    }
}
