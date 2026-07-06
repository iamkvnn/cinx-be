package com.cinx.enrollment.service.enrollment;

import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.EnrolledCourseResponse;
import com.cinx.enrollment.dto.response.UserEnrollmentSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IEnrollmentService {
    Page<EnrolledCourseResponse> getEnrolledCourses(String userId, int page, int size, String query, String sort);
    List<CheckEnrollmentStatus> checkEnrollmentStatus(String userId, List<String> courseIds);
    void enrollCourses(List<CreateEnrolledCourseRequest> requests);
    List<String> getUserIdsEnrolledInCourse(String courseId);
    UserEnrollmentSummaryResponse getUserEnrollmentSummary(String userId);
}
