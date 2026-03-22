package com.cinx.course.dto.response;

public record LectureResponse(
        String id,
        String title,
        String description,
        Long duration,
        Integer orderIndex
) {
}
