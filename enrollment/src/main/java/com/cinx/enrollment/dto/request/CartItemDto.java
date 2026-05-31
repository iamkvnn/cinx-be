package com.cinx.enrollment.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;


import com.cinx.enrollment.dto.response.CourseResponse;

public record CartItemDto (
    @NotBlank(message = "id must not be blank")
    @Schema(example = "cart_item_123")
    String id,

    @NotNull(message = "course must not be null")
    CourseResponse course
) { }
