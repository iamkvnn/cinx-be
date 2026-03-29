package com.cinx.course.dto.response;

public record AssignmentAttachmentResponse(
        String id,
        String fileName,
        String fileType,
        Long fileSize,
        String attachmentUrl,
        String s3ObjectKey
) {
}
