package com.shadowengineer.api.interpretation;

import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report")
public class InterpretationController {

    private final InterpretationService interpretationService;

    public InterpretationController(InterpretationService interpretationService) {
        this.interpretationService = interpretationService;
    }

    @GetMapping("/interpretation")
    public InterpretationResponse interpretation(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return interpretationService.interpret((UUID) userId);
    }
}
