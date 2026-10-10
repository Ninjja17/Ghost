package com.shadowengineer.api.profile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryUserProfileRepository implements UserProfileRepository {

    private final Map<UUID, UserProfile> profiles = new ConcurrentHashMap<>();

    @Override
    public UserProfile save(UserProfile profile) {
        profiles.put(profile.id(), profile);
        return profile;
    }

    @Override
    public Optional<UserProfile> findById(UUID id) {
        return Optional.ofNullable(profiles.get(id));
    }
}
