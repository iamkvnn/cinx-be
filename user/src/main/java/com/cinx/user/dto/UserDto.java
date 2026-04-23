package com.cinx.user.dto;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;
import com.cinx.user.consts.UserStatus;

public record UserDto(String userId, String name, String email, Role role, Gender gender, Boolean isReceivePushNotification, Boolean isInstructorVerified, UserStatus status, String avatarUrl, Integer xp, String cvUrl) {
}
