package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoNoteDto {
    @Schema(example = "note_123")
    private String id;
    @Schema(example = "user_123")
    private String userId;
    @Schema(example = "course_123")
    private String courseId;
    @Schema(example = "les_123")
    private String lessonId;
    @Schema(example = "Important topic to remember.")
    private String content;
    @Schema(example = "120")
    private Integer videoTimestamp;
    @Schema(example = "2025-01-01T10:00:00")
    private LocalDateTime createdAt;
    @Schema(example = "2025-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
