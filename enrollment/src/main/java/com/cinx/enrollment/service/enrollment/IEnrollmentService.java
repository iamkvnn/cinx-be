package com.cinx.enrollment.service.enrollment;

import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IEnrollmentService {
    Page<CourseResponse> getEnrolledCourses(String userId, int page, int size);
    List<CheckEnrollmentStatus> checkEnrollmentStatus(String userId, List<String> courseIds);
}
