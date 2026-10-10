package com.shadowengineer.api.report;

import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ReportResponse report(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return reportService.getForUser((UUID) userId);
    }
}
