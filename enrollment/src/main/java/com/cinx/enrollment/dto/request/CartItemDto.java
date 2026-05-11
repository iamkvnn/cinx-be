package com.cinx.enrollment.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;


import com.cinx.enrollment.dto.response.CourseResponse;

public record CartItemDto (
    @NotBlank(message = "id must not be blank")
    String id,

    @NotNull(message = "course must not be null")
    CourseResponse course
) { }
