package com.cinx.learning.dto.response;

import com.cinx.learning.consts.DailyGoalType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DailyGoalResponse(
    @Schema(example = "goal_123")
    String id,
    @Schema(example = "user_123")
    String userId,
    @Schema(example = "WATCH_VIDEO")
    DailyGoalType goalType,
    @Schema(example = "5")
    Integer targetValue,
    @Schema(example = "3")
    Integer currentValue,
    @Schema(example = "2025-01-01")
    LocalDate goalDate,
    @Schema(example = "item_123")
    String targetItemId,
    @Schema(example = "false")
    Boolean isCompleted
) {}
