package com.cinx.learning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathItemRequest {
    private String courseId;
    private String lessonId;
    private Integer orderIndex;
    private Boolean isSuggested;
}
