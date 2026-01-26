package com.cinx.auth.dto;

public record AuthResponse(TokenResponseDto token, UserDto user) {
}
