package com.cinx.course.dto.request;

public record UpdateInstructorRequest(
        String name,
        String email,
        String bio
) {
}
