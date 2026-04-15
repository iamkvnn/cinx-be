package com.cinx.course.dto.response;

public record RejectCourseResponse(
        String courseId,
        String reason
) {
}
