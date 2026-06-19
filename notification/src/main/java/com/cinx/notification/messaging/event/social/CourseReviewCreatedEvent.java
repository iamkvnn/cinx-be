package com.cinx.notification.messaging.event.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewCreatedEvent {
    private String eventId;
    private String reviewId;
    private String courseId;
    private String reviewerUserId;
    private Double rating;
    private String content;
    private Instant occurredAt;
}
