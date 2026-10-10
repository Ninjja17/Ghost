package com.shadowengineer.api.analysis;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryScoreSnapshotRepository implements ScoreSnapshotRepository {

    private final Map<UUID, ScoreSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public ScoreSnapshot save(ScoreSnapshot snapshot) {
        snapshots.put(snapshot.userId(), snapshot);
        return snapshot;
    }

    @Override
    public Optional<ScoreSnapshot> findByUserId(UUID userId) {
        return Optional.ofNullable(snapshots.get(userId));
    }
}
