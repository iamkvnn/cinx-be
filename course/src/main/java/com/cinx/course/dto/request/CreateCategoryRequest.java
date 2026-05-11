package com.cinx.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
    @NotBlank(message = "Name is required")
    String name
)  {
}
