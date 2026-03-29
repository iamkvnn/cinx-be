package com.cinx.course.dto.response;

import com.cinx.course.model.Course;

public record CourseAggregate (
        Course course,
        UserDto instructor
) {
}
