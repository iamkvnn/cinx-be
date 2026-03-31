package com.cinx.user.dto;

import com.cinx.user.consts.Gender;
import com.cinx.user.consts.Role;

public record UserDto(String userId, String name, String email, Role role, Gender gender, String avatarUrl, Integer xp) {
}
