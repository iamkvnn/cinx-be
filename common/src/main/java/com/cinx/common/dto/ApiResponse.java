package com.cinx.common.dto;


public record ApiResponse (boolean success, String message, Object data) {
}
