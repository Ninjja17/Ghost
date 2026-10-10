package com.shadowengineer.api.auth;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryAuthUserRepository implements AuthUserRepository {

    private final Map<UUID, AuthUser> users = new ConcurrentHashMap<>();

    @Override
    public AuthUser save(AuthUser user) {
        users.put(user.id(), user);
        return user;
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return users.values().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }
}
