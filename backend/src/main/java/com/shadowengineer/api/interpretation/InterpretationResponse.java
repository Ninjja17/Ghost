package com.shadowengineer.api.interpretation;

import java.util.List;

public record InterpretationResponse(
        String interpretationVersion,
        List<Recommendation> recommendations,
        List<MentorInsight> mentors
) {
}
