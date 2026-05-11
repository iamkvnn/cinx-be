package com.cinx.learning.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;


import java.util.List;

public record CreateAssignmentSubmissionRequest(
    @NotBlank(message = "content must not be blank")
    String content,

    @NotEmpty(message = "attachments must not be empty") @Valid
    List<AttachmentRequest> attachments
) {
    public record AttachmentRequest(
        @NotBlank(message = "fileKey must not be blank")
        String fileKey,

        @NotBlank(message = "fileName must not be blank")
        String fileName,

        @NotBlank(message = "fileType must not be blank")
        String fileType,

        @NotNull(message = "fileSize must not be null")
        @Min(value = 1, message = "fileSize must be non-negative")
        Long fileSize
) {}
}
