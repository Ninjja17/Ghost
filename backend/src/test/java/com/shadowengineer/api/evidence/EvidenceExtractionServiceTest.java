package com.shadowengineer.api.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.github.GitHubConnection;
import com.shadowengineer.api.github.GitHubRepositoryRecord;
import com.shadowengineer.api.github.GitHubRepositoryRecordRepository;
import org.junit.jupiter.api.Test;

class EvidenceExtractionServiceTest {

    @Test
    void rebuildsDeterministicFactsFromRepositorySnapshot() {
        UUID userId = UUID.randomUUID();
        UUID connectionId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        GitHubConnection connection = new GitHubConnection(connectionId, userId, 42L, "test-user", now, now);
        GitHubRepositoryRecord repository = new GitHubRepositoryRecord(
                UUID.randomUUID(), connectionId, "100", "sample-api", "https://github.com/test-user/sample-api",
                "Java", 4, 1, 128, false, now.minusDays(4), now
        );
        InMemoryEvidenceRecordRepository evidenceRepository = new InMemoryEvidenceRecordRepository();
        GitHubRepositoryRecordRepository repositoryRepository = new GitHubRepositoryRecordRepository() {
            @Override
            public void replaceSnapshot(UUID ignored, List<GitHubRepositoryRecord> ignoredRecords) {
            }

            @Override
            public List<GitHubRepositoryRecord> findByConnectionId(UUID ignored) {
                return List.of(repository);
            }
        };

        EvidenceExtractionService service = new EvidenceExtractionService(repositoryRepository, evidenceRepository);
        List<EvidenceRecord> records = service.rebuild(connection);

        assertThat(records).extracting(EvidenceRecord::evidenceType)
                .contains("project_count", "active_project_count", "public_reach", "language_signal");
        assertThat(records).anySatisfy(record -> assertThat(record.value()).isEqualTo("Java:1"));
        assertThat(evidenceRepository.findByUserId(userId)).hasSize(4);
    }
}
