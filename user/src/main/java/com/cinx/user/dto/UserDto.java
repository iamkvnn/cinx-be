package com.cinx.user.dto;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;
import com.cinx.user.consts.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record UserDto(
        @Schema(example = "user_123") String userId, 
        @Schema(example = "John Doe") String name, 
        @Schema(example = "johndoe@example.com") String email, 
        @Schema(example = "STUDENT") Role role, 
        @Schema(example = "MALE") Gender gender, 
        @Schema(example = "true") Boolean isReceivePushNotification, 
        @Schema(example = "false") Boolean isInstructorVerified, 
        @Schema(example = "ACTIVE") UserStatus status, 
        @Schema(example = "https://example.com/avatar.jpg") String avatarUrl, 
        @Schema(example = "0987654321") String phoneNumber,
        @Schema(example = "Senior Frontend Developer") String bio,
        @Schema(example = "100") Integer xp, 
        @Schema(example = "https://example.com/cv.pdf") String cvUrl,
        @Schema(example = "2025-01-01T10:00:00") LocalDateTime createdAt,
        @Schema(example = "2025-01-01T10:00:00") LocalDateTime updatedAt,
        @Schema(example = "2025-01-01T10:00:00") LocalDateTime lastAccessAt,
        @Schema(example = "2025-01-01T10:00:00") LocalDateTime instructorVerifiedAt
) {
}
