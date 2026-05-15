package com.cinx.auth.dto.request;

import com.cinx.auth.consts.BanReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BanUserRequest(
        @NotNull(message = "Reason type is required")
        BanReasonType reasonType,
        
        @NotBlank(message = "Details are required")
        String details,
        
        Integer durationDays
) {
}
