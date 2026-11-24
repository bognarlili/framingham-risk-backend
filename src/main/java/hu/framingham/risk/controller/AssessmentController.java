package hu.framingham.risk.controller;

import hu.framingham.risk.dto.request.RiskCaseRequest;
import hu.framingham.risk.dto.response.AssessmentResponse;
import hu.framingham.risk.service.AssessmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Risk estimate for the frontend (lab values in mmol/L). Open to guests; with a valid access
 * token the result is also saved to the user's journal ({@code saved = true}).
 */
@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Validated
public class AssessmentController {
    private final AssessmentService service;

    @PostMapping
    public AssessmentResponse assess(@Valid @RequestBody RiskCaseRequest request,
            @RequestParam(defaultValue = "logistic-regression-full") @Size(max = 64) String modelKey,
            @AuthenticationPrincipal Jwt user) {
        return this.service.assess(request, modelKey, user == null ? null : user.getSubject());
    }
}
