package com.shadowengineer.api.interpretation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.analysis.InMemoryScoreSnapshotRepository;
import com.shadowengineer.api.analysis.ScoreSnapshot;
import com.shadowengineer.api.evidence.EvidenceRecord;
import com.shadowengineer.api.evidence.InMemoryEvidenceRecordRepository;
import org.junit.jupiter.api.Test;

class InterpretationServiceTest {

    @Test
    void recommendationsAndMentorsReferenceObservedEvidence() {
        UUID userId = UUID.randomUUID();
        UUID connectionId = UUID.randomUUID();
        OffsetDateTime observedAt = OffsetDateTime.now(ZoneOffset.UTC);
        UUID projectEvidenceId = UUID.randomUUID();
        InMemoryEvidenceRecordRepository evidenceRepository = new InMemoryEvidenceRecordRepository();
        evidenceRepository.replaceForConnection(connectionId, List.of(
                new EvidenceRecord(projectEvidenceId, userId, connectionId, "project_count", "Projects", "4", 100, "test", observedAt),
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "active_project_count", "Active", "1", 94, "test", observedAt),
                new EvidenceRecord(UUID.randomUUID(), userId, connectionId, "language_signal", "Language", "Java:2", 90, "test", observedAt)
        ));
        InMemoryScoreSnapshotRepository scoreRepository = new InMemoryScoreSnapshotRepository();
        scoreRepository.save(new ScoreSnapshot(UUID.randomUUID(), userId, connectionId, 55, 28, 42, 36, 20, "test", observedAt));

        InterpretationResponse response = new InterpretationService(evidenceRepository, scoreRepository).interpret(userId);

        assertThat(response.interpretationVersion()).isEqualTo("deterministic-interpretation-v1");
        assertThat(response.recommendations()).isNotEmpty();
        assertThat(response.mentors()).hasSize(4);
        assertThat(response.recommendations().get(0).evidenceIds()).contains(projectEvidenceId);
        assertThat(response.mentors()).allSatisfy(mentor -> assertThat(mentor.confidence()).isBetween(0, 100));
    }
}
