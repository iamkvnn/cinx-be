package com.cinx.notification.dto.response.course;

public record CourseResponse(
        String id,
        String title,
        InstructorResponse instructor
) {}