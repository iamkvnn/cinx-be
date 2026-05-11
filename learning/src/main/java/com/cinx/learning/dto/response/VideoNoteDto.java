package com.cinx.learning.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VideoNoteDto {
    private String id;
    private String userId;
    private String courseId;
    private String lessonId;
    private String content;
    private Integer videoTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
