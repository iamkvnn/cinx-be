package com.cinx.learning.dto.request;

import com.cinx.learning.consts.DailyGoalType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SetDailyGoalRequest(
    @NotNull(message = "goalType is required")
    DailyGoalType goalType,

    @Min(value = 1, message = "targetValue must be at least 1")
    Integer targetValue,
    
    LocalDate goalDate,

    String targetItemId
) {}
