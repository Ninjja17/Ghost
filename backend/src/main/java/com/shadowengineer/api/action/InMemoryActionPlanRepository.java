package com.shadowengineer.api.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!postgres")
public class InMemoryActionPlanRepository implements ActionPlanRepository {

    private final Map<UUID, List<ActionPlanItem>> plans = new ConcurrentHashMap<>();

    @Override
    public List<ActionPlanItem> findByUserId(UUID userId) {
        return new ArrayList<>(plans.getOrDefault(userId, List.of()));
    }

    @Override
    public ActionPlanItem save(ActionPlanItem item) {
        plans.computeIfAbsent(item.userId(), ignored -> new ArrayList<>()).removeIf(existing -> existing.id().equals(item.id()));
        plans.computeIfAbsent(item.userId(), ignored -> new ArrayList<>()).add(item);
        return item;
    }
}
