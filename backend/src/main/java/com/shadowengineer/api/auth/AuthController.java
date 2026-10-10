package com.shadowengineer.api.auth;

import java.time.OffsetDateTime;

import com.shadowengineer.api.profile.UserProfile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(AuthService authService,
                          @Value("${app.auth-cookie-secure:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request.email(), request.password(), request.name(),
                request.targetRole(), request.experienceLevel(), request.growthGoal());
        setSessionCookie(response, result.token(), result.expiresAt());
        return toResponse(result);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request.email(), request.password());
        setSessionCookie(response, result.token(), result.expiresAt());
        return toResponse(result);
    }

    @GetMapping("/me")
    public UserProfile me(HttpServletRequest request) {
        return authService.requireProfile(requireToken(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(requireToken(request));
        ResponseCookie cookie = ResponseCookie.from("shadow_engineer_session", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/api")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String requireToken(HttpServletRequest request) {
        Object token = request.getAttribute(AuthTokenFilter.AUTH_TOKEN_ATTRIBUTE);
        if (token == null) throw new InvalidCredentialsException();
        return (String) token;
    }

    private AuthResponse toResponse(AuthService.AuthResult result) {
        return new AuthResponse(result.expiresAt(), result.profile());
    }

    private void setSessionCookie(HttpServletResponse response, String token, OffsetDateTime expiresAt) {
        ResponseCookie cookie = ResponseCookie.from("shadow_engineer_session", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/api")
                .maxAge(java.time.Duration.between(OffsetDateTime.now(), expiresAt))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public record AuthResponse(OffsetDateTime expiresAt, UserProfile profile) {
    }

    public record RegisterRequest(
            @Email @NotBlank @Size(max = 320) String email,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 120) String targetRole,
            @NotBlank @Size(max = 40) String experienceLevel,
            @Size(max = 500) String growthGoal
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }
}
