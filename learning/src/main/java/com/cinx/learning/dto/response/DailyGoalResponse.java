package com.cinx.learning.dto.response;

import java.time.LocalDate;

public record DailyGoalResponse(
    String id,
    String userId,
    Integer targetXp,
    Integer currentXp,
    LocalDate goalDate,
    Boolean isCompleted
) {}