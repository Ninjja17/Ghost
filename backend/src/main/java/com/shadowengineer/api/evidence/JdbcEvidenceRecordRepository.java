package com.shadowengineer.api.evidence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("postgres")
public class JdbcEvidenceRecordRepository implements EvidenceRecordRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcEvidenceRecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void replaceForConnection(UUID connectionId, List<EvidenceRecord> records) {
        jdbcTemplate.update("delete from evidence_records where connection_id = ?", connectionId);
        String sql = """
                insert into evidence_records
                    (id, user_id, connection_id, evidence_type, claim, value, confidence, extractor_version, observed_at)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, records, records.size(), (statement, record) -> {
            statement.setObject(1, record.id());
            statement.setObject(2, record.userId());
            statement.setObject(3, record.connectionId());
            statement.setString(4, record.evidenceType());
            statement.setString(5, record.claim());
            statement.setString(6, record.value());
            statement.setInt(7, record.confidence());
            statement.setString(8, record.extractorVersion());
            statement.setObject(9, record.observedAt());
        });
    }

    @Override
    public List<EvidenceRecord> findByUserId(UUID userId) {
        return jdbcTemplate.query("select * from evidence_records where user_id = ? order by evidence_type, claim",
                this::mapRecord, userId);
    }

    private EvidenceRecord mapRecord(ResultSet resultSet, int rowNumber) throws SQLException {
        return new EvidenceRecord(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getObject("connection_id", UUID.class),
                resultSet.getString("evidence_type"),
                resultSet.getString("claim"),
                resultSet.getString("value"),
                resultSet.getInt("confidence"),
                resultSet.getString("extractor_version"),
                resultSet.getObject("observed_at", OffsetDateTime.class)
        );
    }
}
