package com.shadowengineer.api.evidence;

import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class EvidenceController {

    private final EvidenceExtractionService extractionService;

    public EvidenceController(EvidenceExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    @GetMapping("/evidence")
    public List<EvidenceRecord> evidence(HttpServletRequest request) {
        return extractionService.findForUser(authenticatedUserId(request));
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return (UUID) userId;
    }
}
