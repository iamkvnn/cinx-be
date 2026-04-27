package com.cinx.course.dto.request;

public record UpdateSectionRequest(
        String title,
        String description,
        Long duration,
        Integer orderIndex
) {
}
