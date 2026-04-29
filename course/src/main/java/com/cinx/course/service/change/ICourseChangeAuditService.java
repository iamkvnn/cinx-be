package com.cinx.course.service.change;

import com.cinx.course.dto.response.CourseChangeResponse;
import com.cinx.course.dto.response.CourseResponse;

import java.util.List;

public interface ICourseChangeAuditService {
    void auditCourseChange(String courseId, CourseResponse oldValue, CourseResponse newValue);
    void auditCourseItemChange(String courseId, String itemId, Object oldValue, Object newValue);
    List<CourseChangeResponse> getCourseChangeHistory(String courseId);
    void deleteCourseChangeHistory(String courseId);
}
