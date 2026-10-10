package com.shadowengineer.api.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.evidence.EvidenceRecord;
import com.shadowengineer.api.evidence.EvidenceRecordRepository;
import org.junit.jupiter.api.Test;

class ScoringServiceTest {

    @Test
    void calculatesScoresFromPersistedEvidence() {
        UUID userId = UUID.randomUUID();
        UUID connectionId = UUID.randomUUID();
        OffsetDateTime observedAt = OffsetDateTime.now(ZoneOffset.UTC);
        List<EvidenceRecord> evidence = List.of(
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "project_count", "Projects", "4", 100, "test", observedAt),
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "active_project_count", "Active", "2", 94, "test", observedAt),
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "public_reach", "Stars", "10", 88, "test", observedAt),
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "language_signal", "Language", "Java:2", 90, "test", observedAt)
        );
        EvidenceRecordRepository evidenceRepository = new EvidenceRecordRepository() {
            @Override
            public void replaceForConnection(UUID ignored, List<EvidenceRecord> ignoredRecords) {
            }

            @Override
            public List<EvidenceRecord> findByUserId(UUID ignored) {
                return evidence;
            }
        };
        InMemoryScoreSnapshotRepository scoreRepository = new InMemoryScoreSnapshotRepository();

        ScoreSnapshot snapshot = new ScoringService(evidenceRepository, scoreRepository).rebuild(userId, connectionId);

        assertThat(snapshot.builderScore()).isEqualTo(55);
        assertThat(snapshot.finisherScore()).isEqualTo(36);
        assertThat(snapshot.learnerScore()).isEqualTo(42);
        assertThat(snapshot.architectScore()).isEqualTo(42);
        assertThat(snapshot.collaboratorScore()).isEqualTo(20);
        assertThat(snapshot.formulaVersion()).isEqualTo("github-evidence-v1");
    }
}
