package com.shadowengineer.api.action;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ActionPlanService {

    private final ActionPlanRepository repository;

    public ActionPlanService(ActionPlanRepository repository) {
        this.repository = repository;
    }

    public List<ActionPlanItem> list(UUID userId) {
        return repository.findByUserId(userId);
    }

    public List<ActionPlanItem> generate(UUID userId) {
        if (!repository.findByUserId(userId).isEmpty()) return repository.findByUserId(userId);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<ActionPlanItem> items = List.of(
                item(userId, 1, "Choose one project to finish", "Select the project with the clearest user outcome and define its release scope.", "A written scope and a public repository are ready.", now),
                item(userId, 2, "Add tests and documentation", "Add a happy-path test suite and a setup guide another developer can follow.", "Tests pass and a new developer can run the project locally.", now),
                item(userId, 3, "Containerize and deploy", "Create a Docker image and deploy the project to a reachable environment.", "A public deployment URL responds successfully.", now),
                item(userId, 4, "Publish the engineering story", "Document the architecture decision, tradeoffs, and measurable user outcome.", "README includes architecture, tradeoffs, and outcome evidence.", now)
        );
        items.forEach(repository::save);
        return items;
    }

    public ActionPlanItem updateStatus(UUID userId, UUID itemId, String status) {
        ActionPlanItem item = repository.findByUserId(userId).stream()
                .filter(candidate -> candidate.id().equals(itemId))
                .findFirst()
                .orElseThrow();
        if (!List.of("pending", "in_progress", "completed", "skipped").contains(status)) {
            throw new IllegalArgumentException("Unsupported action-plan status.");
        }
        ActionPlanItem updated = new ActionPlanItem(item.id(), item.userId(), item.weekNumber(), item.title(), item.details(),
                item.completionCriteria(), status, item.createdAt(), OffsetDateTime.now(ZoneOffset.UTC));
        return repository.save(updated);
    }

    private ActionPlanItem item(UUID userId, int week, String title, String details, String criteria, OffsetDateTime now) {
        return new ActionPlanItem(UUID.randomUUID(), userId, week, title, details, criteria, "pending", now, now);
    }
}
