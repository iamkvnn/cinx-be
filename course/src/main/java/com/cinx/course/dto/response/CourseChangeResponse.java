package com.cinx.course.dto.response;

public record CourseChangeResponse(
        String courseId,
        String itemId,
        String oldValue,
        String newValue
) {
}
