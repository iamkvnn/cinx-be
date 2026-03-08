package com.cinx.enrollment.dto.request;

import com.cinx.enrollment.dto.response.CourseResponse;

public record CartItemDto (
    String id,
    CourseResponse course
) { }
