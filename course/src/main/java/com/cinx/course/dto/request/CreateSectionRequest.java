package com.cinx.course.dto.request;

public record CreateSectionRequest(
        String title,
        String description,
        Long duration,
        Integer orderIndex
) {
}
