package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final IEnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getEnrolledCourses(@RequestHeader("X-User-Id") String userId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(PaginationWrapper.wrap(enrollmentService.getEnrolledCourses(userId, page, size)));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<?>> checkEnrollmentStatus(@RequestHeader("X-User-Id") String userId, @RequestBody List<String> courseIds) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", enrollmentService.checkEnrollmentStatus(userId, courseIds)));
    }
}
