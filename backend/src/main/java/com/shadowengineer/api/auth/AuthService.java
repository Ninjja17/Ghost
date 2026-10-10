package com.shadowengineer.api.auth;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import com.shadowengineer.api.profile.UserProfile;
import com.shadowengineer.api.profile.UserProfileRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final UserProfileRepository profileRepository;
    private final SessionStore sessionStore;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthUserRepository authUserRepository,
                       UserProfileRepository profileRepository,
                       SessionStore sessionStore) {
        this.authUserRepository = authUserRepository;
        this.profileRepository = profileRepository;
        this.sessionStore = sessionStore;
    }

    public AuthResult register(String email, String password, String name, String targetRole,
                               String experienceLevel, String growthGoal) {
        String normalizedEmail = normalizeEmail(email);
        if (authUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyRegisteredException();
        }
        AuthUser user = new AuthUser(UUID.randomUUID(), normalizedEmail, passwordEncoder.encode(password), now());
        authUserRepository.save(user);
        UserProfile profile = new UserProfile(user.id(), name, normalizedEmail, targetRole,
                experienceLevel, growthGoal, now(), now());
        profileRepository.save(profile);
        return issueSession(user, profile);
    }

    public AuthResult login(String email, String password) {
        AuthUser user = authUserRepository.findByEmail(normalizeEmail(email))
                .filter(candidate -> passwordEncoder.matches(password, candidate.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        UserProfile profile = profileRepository.findById(user.id()).orElseThrow(InvalidCredentialsException::new);
        return issueSession(user, profile);
    }

    public UserProfile requireProfile(String token) {
        UUID userId = sessionStore.find(token).orElseThrow(InvalidCredentialsException::new).userId();
        return profileRepository.findById(userId).orElseThrow(InvalidCredentialsException::new);
    }

    public void logout(String token) {
        sessionStore.delete(token);
    }

    private AuthResult issueSession(AuthUser user, UserProfile profile) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SessionStore.Session session = sessionStore.create(user.id(), token);
        return new AuthResult(token, session.expiresAt(), profile);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public record AuthResult(String token, OffsetDateTime expiresAt, UserProfile profile) {
    }
}
