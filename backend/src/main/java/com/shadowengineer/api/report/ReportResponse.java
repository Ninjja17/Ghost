package com.shadowengineer.api.report;

import java.util.List;

import com.shadowengineer.api.analysis.ScoreSnapshot;
import com.shadowengineer.api.evidence.EvidenceRecord;
import com.shadowengineer.api.github.GitHubConnection;
import com.shadowengineer.api.github.GitHubRepositoryRecord;
import com.shadowengineer.api.profile.UserProfile;

public record ReportResponse(
        UserProfile profile,
        GitHubConnection source,
        List<GitHubRepositoryRecord> repositories,
        List<EvidenceRecord> evidence,
        ScoreSnapshot scores
) {
}
