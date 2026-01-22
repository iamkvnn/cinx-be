package com.cinx.auth.dto;


public record ApiResponse (boolean success, String message, Object data) {
}
