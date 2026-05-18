package com.cinx.learning.dto.response;

import com.cinx.learning.consts.DailyGoalType;

import java.time.LocalDate;

public record DailyGoalResponse(
    String id,
    String userId,
    DailyGoalType goalType,
    Integer targetValue,
    Integer currentValue,
    LocalDate goalDate,
    String targetItemId,
    Boolean isCompleted
) {}
