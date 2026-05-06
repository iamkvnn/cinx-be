package com.cinx.learning.service.enrollment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.request.CreateEnrolledCourseRequest;
import com.cinx.learning.dto.response.CheckEnrollmentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "enrollment", path = "/internal")
public interface EnrollmentClient {

    @PostMapping("/enrollments/check")
    ApiResponse<List<CheckEnrollmentStatus>> checkEnrollmentStatus(@RequestBody List<String> courseIds);

    @PostMapping("/enrollments")
    ApiResponse<Void> enrollCourses(@RequestBody List<CreateEnrolledCourseRequest> requests);

    @GetMapping("/enrollments/courses/{courseId}/users")
    ApiResponse<List<String>> getUserIdsEnrolledInCourse(@PathVariable("courseId") String courseId);
}
