package com.cinx.user.dto;

import com.cinx.user.consts.Gender;

public record UserDto(String userId, String name, String email, Gender gender, String avatarUrl) {
}
