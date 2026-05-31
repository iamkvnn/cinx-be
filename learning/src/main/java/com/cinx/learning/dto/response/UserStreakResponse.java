package com.cinx.learning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakResponse {
    @Schema(example = "user_123")
    private String userId;
    @Schema(example = "5")
    private Integer currentStreak;
    @Schema(example = "15")
    private Integer highestStreak;
    @Schema(example = "2025-01-01")
    private LocalDate lastActivityDate;
}
