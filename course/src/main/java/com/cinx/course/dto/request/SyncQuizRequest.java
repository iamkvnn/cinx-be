package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotNull;

public record SyncQuizRequest(
        @NotNull Boolean triggerRegrade,
        String changeReason
) {}
