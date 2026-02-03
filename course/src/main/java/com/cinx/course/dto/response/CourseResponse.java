package com.cinx.course.dto.response;

public record CourseResponse (
        String id,
        String title,
        String description,
        String category,
        Double price,
        Long duration
) {
}
