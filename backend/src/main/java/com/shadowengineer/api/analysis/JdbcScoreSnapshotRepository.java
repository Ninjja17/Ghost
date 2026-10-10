package com.shadowengineer.api.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcScoreSnapshotRepository implements ScoreSnapshotRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcScoreSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ScoreSnapshot save(ScoreSnapshot snapshot) {
        String sql = """
                insert into score_snapshots
                    (id, user_id, connection_id, builder_score, finisher_score, learner_score, architect_score, collaborator_score, formula_version, observed_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (user_id) do update set
                    connection_id = excluded.connection_id,
                    builder_score = excluded.builder_score,
                    finisher_score = excluded.finisher_score,
                    learner_score = excluded.learner_score,
                    architect_score = excluded.architect_score,
                    collaborator_score = excluded.collaborator_score,
                    formula_version = excluded.formula_version,
                    observed_at = excluded.observed_at
                """;
        jdbcTemplate.update(sql, snapshot.id(), snapshot.userId(), snapshot.connectionId(), snapshot.builderScore(),
                snapshot.finisherScore(), snapshot.learnerScore(), snapshot.architectScore(), snapshot.collaboratorScore(),
                snapshot.formulaVersion(), snapshot.observedAt());
        return findByUserId(snapshot.userId()).orElse(snapshot);
    }

    @Override
    public Optional<ScoreSnapshot> findByUserId(UUID userId) {
        return jdbcTemplate.query("select * from score_snapshots where user_id = ?", this::mapSnapshot, userId)
                .stream()
                .findFirst();
    }

    private ScoreSnapshot mapSnapshot(ResultSet resultSet, int rowNumber) throws SQLException {
        return new ScoreSnapshot(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getObject("connection_id", UUID.class),
                resultSet.getInt("builder_score"),
                resultSet.getInt("finisher_score"),
                resultSet.getInt("learner_score"),
                resultSet.getInt("architect_score"),
                resultSet.getInt("collaborator_score"),
                resultSet.getString("formula_version"),
                resultSet.getObject("observed_at", OffsetDateTime.class)
        );
    }
}
