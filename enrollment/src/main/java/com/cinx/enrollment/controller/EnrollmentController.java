package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final IEnrollmentService enrollmentService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getEnrolledCourses(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(enrollmentService.getEnrolledCourses(page, size)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<List<CheckEnrollmentStatus>>> checkEnrollmentStatus(@RequestBody List<String> courseIds) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", enrollmentService.checkEnrollmentStatus(courseIds)));
    }
}
