package com.cinx.enrollment.service.enrollment;

import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IEnrollmentService {
    Page<CourseResponse> getEnrolledCourses(int page, int size);
    List<CheckEnrollmentStatus> checkEnrollmentStatus(List<String> courseIds);
    void enrollCourses(List<CreateEnrolledCourseRequest> requests);
}
