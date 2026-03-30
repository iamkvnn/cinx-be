package com.cinx.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemResponse {
    private String id;
    private String courseId;
    private String lessonId;
    private Integer orderIndex;
    private Boolean isSuggested;
    private Boolean isCompleted;
}
