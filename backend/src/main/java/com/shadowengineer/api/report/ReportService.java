package com.shadowengineer.api.report;

import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.analysis.ScoreSnapshot;
import com.shadowengineer.api.analysis.ScoreSnapshotRepository;
import com.shadowengineer.api.evidence.EvidenceRecordRepository;
import com.shadowengineer.api.github.GitHubConnection;
import com.shadowengineer.api.github.GitHubConnectionRepository;
import com.shadowengineer.api.github.GitHubRepositoryRecordRepository;
import com.shadowengineer.api.profile.UserProfile;
import com.shadowengineer.api.profile.UserProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final UserProfileRepository profileRepository;
    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRecordRepository repositoryRecordRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final ScoreSnapshotRepository scoreSnapshotRepository;

    public ReportService(UserProfileRepository profileRepository,
                         GitHubConnectionRepository connectionRepository,
                         GitHubRepositoryRecordRepository repositoryRecordRepository,
                         EvidenceRecordRepository evidenceRecordRepository,
                         ScoreSnapshotRepository scoreSnapshotRepository) {
        this.profileRepository = profileRepository;
        this.connectionRepository = connectionRepository;
        this.repositoryRecordRepository = repositoryRecordRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.scoreSnapshotRepository = scoreSnapshotRepository;
    }

    public ReportResponse getForUser(UUID userId) {
        UserProfile profile = profileRepository.findById(userId).orElseThrow();
        GitHubConnection source = connectionRepository.findByUserId(userId).orElse(null);
        if (source == null) return new ReportResponse(profile, null, List.of(), List.of(), null);
        List<com.shadowengineer.api.github.GitHubRepositoryRecord> repositories = repositoryRecordRepository.findByConnectionId(source.id());
        List<com.shadowengineer.api.evidence.EvidenceRecord> evidence = evidenceRecordRepository.findByUserId(userId);
        ScoreSnapshot scores = scoreSnapshotRepository.findByUserId(userId).orElse(null);
        return new ReportResponse(profile, source, repositories, evidence, scores);
    }
}
