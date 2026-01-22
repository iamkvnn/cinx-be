package com.cinx.auth.dto;

import com.cinx.auth.consts.Gender;

public record RegisterDto (String name, String email, String password, Gender gender) {
}
