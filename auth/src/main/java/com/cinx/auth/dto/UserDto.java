package com.cinx.auth.dto;

import com.cinx.auth.consts.Gender;
import com.cinx.auth.consts.Role;

public record UserDto(String id, String name, String email, Role role, Gender gender) {
}
