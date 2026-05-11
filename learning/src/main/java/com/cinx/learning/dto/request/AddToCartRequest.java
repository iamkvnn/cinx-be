package com.cinx.learning.dto.request;
import jakarta.validation.constraints.NotBlank;


public record AddToCartRequest(
    @NotBlank(message = "courseId must not be blank")
    String courseId
) {}
