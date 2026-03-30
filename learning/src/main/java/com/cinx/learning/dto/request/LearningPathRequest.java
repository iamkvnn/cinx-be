package com.cinx.learning.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathRequest {
    private String title;
    private String description;
    private List<LearningPathItemRequest> items;
}
