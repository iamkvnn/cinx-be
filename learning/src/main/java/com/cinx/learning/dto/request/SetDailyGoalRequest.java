package com.cinx.learning.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SetDailyGoalRequest(
    @NotNull(message = "targetXp is required")
    @Min(value = 10, message = "targetXp must be at least 10")
    Integer targetXp,
    
    LocalDate goalDate
) {}