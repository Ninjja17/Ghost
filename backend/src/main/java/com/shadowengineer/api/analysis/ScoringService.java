package com.shadowengineer.api.analysis;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.evidence.EvidenceRecord;
import com.shadowengineer.api.evidence.EvidenceRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    private static final String FORMULA_VERSION = "github-evidence-v1";
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final ScoreSnapshotRepository scoreSnapshotRepository;

    public ScoringService(EvidenceRecordRepository evidenceRecordRepository,
                          ScoreSnapshotRepository scoreSnapshotRepository) {
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.scoreSnapshotRepository = scoreSnapshotRepository;
    }

    public ScoreSnapshot rebuild(UUID userId, UUID connectionId) {
        List<EvidenceRecord> evidence = evidenceRecordRepository.findByUserId(userId);
        int projects = value(evidence, "project_count");
        int activeProjects = value(evidence, "active_project_count");
        int stars = value(evidence, "public_reach");
        long languages = evidence.stream().filter(record -> record.evidenceType().equals("language_signal")).count();

        ScoreSnapshot snapshot = new ScoreSnapshot(
                UUID.randomUUID(),
                userId,
                connectionId,
                clamp(35 + projects * 5),
                clamp(20 + activeProjects * 8),
                clamp(30 + (int) languages * 12),
                clamp(30 + activeProjects * 6),
                20,
                FORMULA_VERSION,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        return scoreSnapshotRepository.save(snapshot);
    }

    public ScoreSnapshot findForUser(UUID userId) {
        return scoreSnapshotRepository.findByUserId(userId).orElseThrow(() -> new IllegalStateException("No score snapshot exists yet."));
    }

    private int value(List<EvidenceRecord> evidence, String type) {
        return evidence.stream()
                .filter(record -> record.evidenceType().equals(type))
                .map(EvidenceRecord::value)
                .findFirst()
                .map(Integer::parseInt)
                .orElse(0);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
