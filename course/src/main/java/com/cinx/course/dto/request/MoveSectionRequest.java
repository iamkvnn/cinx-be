package com.cinx.course.dto.request;

public record MoveSectionRequest(
        String previousSectionId,
        String nextSectionId
) {
}
