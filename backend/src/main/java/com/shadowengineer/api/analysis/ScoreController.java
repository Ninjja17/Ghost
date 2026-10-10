package com.shadowengineer.api.analysis;

import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class ScoreController {

    private final ScoringService scoringService;

    public ScoreController(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @GetMapping("/scores")
    public ScoreSnapshot scores(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return scoringService.findForUser((UUID) userId);
    }
}
