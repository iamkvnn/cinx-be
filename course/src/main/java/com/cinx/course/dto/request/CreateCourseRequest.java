package com.cinx.course.dto.request;

public record CreateCourseRequest (
        String title,
        String description,
        String categoryId,
        Long price,
        Long duration
) {
}
