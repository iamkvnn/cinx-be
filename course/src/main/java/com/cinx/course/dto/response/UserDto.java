package com.cinx.course.dto.response;

import com.cinx.course.consts.Gender;

public record UserDto(String userId, String name, String email, Gender gender, String avatarUrl) {
}
