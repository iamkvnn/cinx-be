package com.cinx.learning.dto.response;

public record AssignmentSubmissionAttachmentResponse(
        String id,
        String fileName,
        String fileType,
        long fileSize,
        String attachmentUrl,
        String s3ObjectKey
) {
}
