package com.cinx.auth.dto.request;

import com.cinx.auth.consts.BanReasonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BanUserRequest(
        @NotNull(message = "Reason type is required")
        @Schema(example = "VIOLATION_OF_TERMS")
        BanReasonType reasonType,
        
        @NotBlank(message = "Details are required")
        @Schema(example = "Posting spam links")
        String details,
        
        @Schema(example = "30")
        Integer durationDays
) {
}
