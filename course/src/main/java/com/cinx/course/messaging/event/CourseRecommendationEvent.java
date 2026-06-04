package com.cinx.course.messaging.event;

import java.time.LocalDateTime;

public record CourseRecommendationEvent(
        CourseRecommendationPayload course,
        LocalDateTime timestamp
) {
}
