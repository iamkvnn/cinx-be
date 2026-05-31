package com.cinx.course.service.change;

import com.cinx.course.dto.response.CourseChangeResponse;

import java.util.List;

public interface ICourseChangeAuditService {
    List<CourseChangeResponse> getCourseChangeHistory(String courseId);
}
