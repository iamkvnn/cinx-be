package com.cinx.auth.dto;

public record AuthResponse(String token, UserDto user) {
}
