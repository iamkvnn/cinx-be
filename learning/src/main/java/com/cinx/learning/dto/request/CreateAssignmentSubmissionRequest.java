package com.cinx.learning.dto.request;

import java.util.List;

public record CreateAssignmentSubmissionRequest(
        String content,
        List<AttachmentRequest> attachments
) {
    public record AttachmentRequest(
            String s3ObjectKey,
            String attachmentUrl,
            String fileName,
            String fileType,
            Long fileSize
    ) {}
}
