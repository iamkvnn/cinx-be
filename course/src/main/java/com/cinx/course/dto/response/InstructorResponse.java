package com.cinx.course.dto.response;

import com.cinx.course.consts.Gender;

public record InstructorResponse(
        String id,
        String name,
        String email,
        Gender gender,
        String avatarUrl
) {
}
