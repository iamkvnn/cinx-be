package com.cinx.learning.dto.response;

import com.cinx.learning.consts.LearningPathStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private LearningPathStatus status;
    private Double currentProgress;
    private Integer totalItems;
    private Integer completedItems;
    private List<LearningPathItemResponse> items;
}
