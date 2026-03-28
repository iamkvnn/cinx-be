package com.cinx.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AssignmentSubmissionResponse(
        String id,
        String userId,
        LocalDateTime submissionTime,
        String assignmentId,
        String content,
        Double score,
        String feedback,
        List<AssignmentSubmissionAttachmentResponse> attachments
) {
}
