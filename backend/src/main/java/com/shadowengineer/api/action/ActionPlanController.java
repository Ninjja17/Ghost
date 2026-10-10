package com.shadowengineer.api.action;

import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/action-plan")
public class ActionPlanController {

    private final ActionPlanService service;

    public ActionPlanController(ActionPlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActionPlanItem> list(HttpServletRequest request) {
        return service.list(userId(request));
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ActionPlanItem> generate(HttpServletRequest request) {
        return service.generate(userId(request));
    }

    @PatchMapping("/{id}/status")
    public ActionPlanItem updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusRequest request,
                                       HttpServletRequest servletRequest) {
        return service.updateStatus(userId(servletRequest), id, request.status());
    }

    private UUID userId(HttpServletRequest request) {
        Object value = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (value == null) throw new InvalidCredentialsException();
        return (UUID) value;
    }

    public record StatusRequest(@NotBlank String status) {
    }
}
