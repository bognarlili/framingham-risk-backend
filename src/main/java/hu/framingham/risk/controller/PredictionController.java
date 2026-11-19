package hu.framingham.risk.controller;

import hu.framingham.risk.dto.request.PredictionRequest;
import hu.framingham.risk.dto.response.PredictionResponse;
import hu.framingham.risk.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {
    private final PredictionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PredictionResponse create(@Valid @RequestBody PredictionRequest request) {
        return this.service.create(request);
    }

    @GetMapping
    public Page<PredictionResponse> list(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return this.service.list(pageable);
    }
}
