package com.shadowengineer.api.interpretation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.shadowengineer.api.analysis.ScoreSnapshot;
import com.shadowengineer.api.analysis.ScoreSnapshotRepository;
import com.shadowengineer.api.evidence.EvidenceRecord;
import com.shadowengineer.api.evidence.EvidenceRecordRepository;
import org.springframework.stereotype.Service;

@Service
public class InterpretationService {

    private final EvidenceRecordRepository evidenceRepository;
    private final ScoreSnapshotRepository scoreRepository;

    public InterpretationService(EvidenceRecordRepository evidenceRepository,
                                 ScoreSnapshotRepository scoreRepository) {
        this.evidenceRepository = evidenceRepository;
        this.scoreRepository = scoreRepository;
    }

    public InterpretationResponse interpret(UUID userId) {
        List<EvidenceRecord> evidence = evidenceRepository.findByUserId(userId);
        ScoreSnapshot scores = scoreRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No score snapshot exists yet."));
        List<UUID> activityEvidence = ids(evidence, "active_project_count", "project_count");
        List<UUID> languageEvidence = ids(evidence, "language_signal");
        List<UUID> reachEvidence = ids(evidence, "public_reach");

        List<Recommendation> recommendations = new ArrayList<>();
        if (scores.finisherScore() < 50) {
            recommendations.add(new Recommendation(
                    "completion",
                    "Finish one project in public",
                    "Deploy one existing project, add a setup guide, and publish a short architecture note.",
                    "Your current activity shows building momentum, but completion evidence is weaker than project volume.",
                    84,
                    activityEvidence
            ));
        }
        if (scores.architectScore() < 60) {
            recommendations.add(new Recommendation(
                    "architecture",
                    "Add one production-shaped system signal",
                    "Add tests, containerization, and a deployment path to the project you want employers to inspect.",
                    "The evidence shows active projects but does not yet prove production ownership.",
                    78,
                    activityEvidence
            ));
        }
        if (scores.learnerScore() >= 60) {
            recommendations.add(new Recommendation(
                    "focus",
                    "Turn exploration into a deliberate thread",
                    "Choose one backend capability and apply it deeply across the next project instead of opening another stack.",
                    "Technology diversity is visible; the next growth signal is depth connected to an outcome.",
                    71,
                    languageEvidence
            ));
        }

        List<MentorInsight> mentors = List.of(
                new MentorInsight("engineer", "Code quality and execution",
                        "You have implementation momentum, but another engineer cannot yet see a complete quality loop.",
                        "Add tests and a reproducible local setup to the project you want reviewed.",
                        "What would another engineer need to run this without asking you for help?",
                        76, "Test coverage and review data are not currently collected.", activityEvidence),
                new MentorInsight("architect", "Systems thinking and scale",
                        "Your activity supports growing systems instincts, while deployment and production evidence remain limited.",
                        "Document one architecture decision and validate it through a deployed project.",
                        "Where does this system fail first when traffic, data, or team size grows?",
                        73, "Release, infrastructure, and pull-request evidence are incomplete.", activityEvidence),
                new MentorInsight("recruiter", "Employability and proof",
                        "Your public technology signal is stronger than your public outcome signal.",
                        "Make one repository portfolio-ready with a clear problem, live result, and measurable outcome.",
                        "Can a hiring manager understand the problem you solved in thirty seconds?",
                        79, "Stars and languages are proxies, not employability proof.", reachEvidence),
                new MentorInsight("product", "User value and outcomes",
                        "The evidence shows what you build, but not consistently who benefits from it.",
                        "State one user problem and one success measure before adding another feature.",
                        "Who is better off because this project exists?",
                        68, "Repository metadata does not reveal real user outcomes.", activityEvidence)
        );
        return new InterpretationResponse("deterministic-interpretation-v1", recommendations, mentors);
    }

    private List<UUID> ids(List<EvidenceRecord> evidence, String... types) {
        return evidence.stream()
                .filter(record -> java.util.Arrays.asList(types).contains(record.evidenceType()))
                .map(EvidenceRecord::id)
                .toList();
    }
}
