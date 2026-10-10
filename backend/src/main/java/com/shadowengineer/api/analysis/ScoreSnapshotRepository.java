package com.shadowengineer.api.analysis;

import java.util.Optional;
import java.util.UUID;

public interface ScoreSnapshotRepository {

    ScoreSnapshot save(ScoreSnapshot snapshot);

    Optional<ScoreSnapshot> findByUserId(UUID userId);
}
