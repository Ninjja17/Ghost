package com.shadowengineer.api.profile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private final UserProfileRepository repository;

    public UserProfileController(UserProfileRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfile create(@Valid @RequestBody CreateUserProfileRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        UserProfile profile = new UserProfile(
                UUID.randomUUID(),
                request.name(),
                request.email(),
                request.targetRole(),
                request.experienceLevel(),
                request.growthGoal(),
                now,
                now
        );
        return repository.save(profile);
    }

    @GetMapping("/{id}")
    public UserProfile get(@PathVariable UUID id, HttpServletRequest request) {
        requireOwner(id, request);
        return repository.findById(id).orElseThrow(() -> new UserProfileNotFoundException(id));
    }

    @GetMapping("/me")
    public UserProfile me(HttpServletRequest request) {
        UUID userId = authenticatedUserId(request);
        return repository.findById(userId).orElseThrow(() -> new UserProfileNotFoundException(userId));
    }

    private void requireOwner(UUID requestedId, HttpServletRequest request) {
        if (!requestedId.equals(authenticatedUserId(request))) throw new InvalidCredentialsException();
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return (UUID) userId;
    }

    public record CreateUserProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @Email @Size(max = 320) String email,
            @Size(max = 120) String targetRole,
            @Size(max = 40) String experienceLevel,
            @Size(max = 500) String growthGoal
    ) {
    }
}
