package com.cinx.notification.dto.response.course;

import io.swagger.v3.oas.annotations.media.Schema;

public record InstructorResponse(
        @Schema(example = "inst_123")
        String id,
        @Schema(example = "John Doe")
        String name
) {}