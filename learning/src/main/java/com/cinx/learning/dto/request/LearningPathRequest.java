package com.cinx.learning.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "React Developer Path")
    private String title;
    @Schema(example = "Master React and Redux")
    private String description;
    private List<LearningPathItemRequest> items;
}
