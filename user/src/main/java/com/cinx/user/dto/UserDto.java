package com.cinx.user.dto;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;
import com.cinx.user.consts.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

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
        @Schema(example = "100") Integer xp, 
        @Schema(example = "https://example.com/cv.pdf") String cvUrl
) {
}
