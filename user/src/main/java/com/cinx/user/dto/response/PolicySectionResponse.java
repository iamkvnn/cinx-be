package com.cinx.user.dto.response;

public record PolicySectionResponse(
        String id,
        String heading,
        String anchor,
        String bodyMarkdown,
        Integer orderIndex
) {
}
