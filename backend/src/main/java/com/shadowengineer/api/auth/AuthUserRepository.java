package com.shadowengineer.api.auth;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository {

    AuthUser save(AuthUser user);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findById(UUID id);
}
