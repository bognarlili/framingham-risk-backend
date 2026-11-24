package hu.framingham.risk.controller;

import hu.framingham.risk.dto.response.JournalEntryResponse;
import hu.framingham.risk.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** The logged-in user's journal: saved assessments, newest first. */
@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {
    private final JournalService service;

    @GetMapping
    public Page<JournalEntryResponse> list(@AuthenticationPrincipal Jwt user,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return this.service.list(user.getSubject(), pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt user, @PathVariable UUID id) {
        this.service.delete(user.getSubject(), id);
    }
}
