package hu.framingham.risk.controller;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.AssessmentResponse;
import hu.framingham.risk.service.AssessmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Single call for the frontend: same body as POST /api/cases (lab values in mmol/L).
 * Always 201 because the case is saved; check {@code modelStatus} for the prediction.
 */
@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Validated
public class AssessmentController {
    private final AssessmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssessmentResponse assess(@Valid @RequestBody RiskCaseRequest request,
            @RequestParam(defaultValue = "logistic-regression-full") @Size(max = 64) String modelKey) {
        return this.service.assess(request, modelKey);
    }
}
