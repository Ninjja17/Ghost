package com.shadowengineer.api.action;

import java.util.List;
import java.util.UUID;

public interface ActionPlanRepository {

    List<ActionPlanItem> findByUserId(UUID userId);

    ActionPlanItem save(ActionPlanItem item);
}
