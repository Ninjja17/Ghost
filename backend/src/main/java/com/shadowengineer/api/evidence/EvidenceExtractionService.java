package com.shadowengineer.api.evidence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.shadowengineer.api.github.GitHubConnection;
import com.shadowengineer.api.github.GitHubRepositoryRecord;
import com.shadowengineer.api.github.GitHubRepositoryRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class EvidenceExtractionService {

    private static final String EXTRACTOR_VERSION = "github-basic-v1";

    private final GitHubRepositoryRecordRepository repositoryRecordRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;

    public EvidenceExtractionService(GitHubRepositoryRecordRepository repositoryRecordRepository,
                                     EvidenceRecordRepository evidenceRecordRepository) {
        this.repositoryRecordRepository = repositoryRecordRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
    }

    public List<EvidenceRecord> rebuild(GitHubConnection connection) {
        List<GitHubRepositoryRecord> repositories = repositoryRecordRepository.findByConnectionId(connection.id());
        OffsetDateTime observedAt = OffsetDateTime.now(ZoneOffset.UTC);
        List<EvidenceRecord> records = new ArrayList<>();

        records.add(fact(connection, "project_count", "Public repositories analyzed", String.valueOf(repositories.size()), 100, observedAt));
        records.add(fact(connection, "active_project_count", "Repositories updated in the last six months", String.valueOf(activeCount(repositories)), 94, observedAt));
        records.add(fact(connection, "public_reach", "Total repository stars", String.valueOf(repositories.stream().mapToInt(GitHubRepositoryRecord::stars).sum()), 88, observedAt));

        Map<String, Long> languageCounts = repositories.stream()
                .filter(repository -> repository.language() != null && !repository.language().isBlank())
                .collect(java.util.stream.Collectors.groupingBy(GitHubRepositoryRecord::language, java.util.stream.Collectors.counting()));
        languageCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .forEach(entry -> records.add(fact(connection, "language_signal", "Language detected in repositories", entry.getKey() + ":" + entry.getValue(), 90, observedAt)));

        evidenceRecordRepository.replaceForConnection(connection.id(), records);
        return records;
    }

    public List<EvidenceRecord> findForUser(UUID userId) {
        return evidenceRecordRepository.findByUserId(userId);
    }

    private int activeCount(List<GitHubRepositoryRecord> repositories) {
        OffsetDateTime threshold = OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6);
        return (int) repositories.stream()
                .filter(repository -> repository.updatedAt() != null && repository.updatedAt().isAfter(threshold))
                .count();
    }

    private EvidenceRecord fact(GitHubConnection connection, String type, String claim, String value,
                                int confidence, OffsetDateTime observedAt) {
        return new EvidenceRecord(UUID.randomUUID(), connection.userId(), connection.id(), type, claim, value,
                confidence, EXTRACTOR_VERSION, observedAt);
    }
}
