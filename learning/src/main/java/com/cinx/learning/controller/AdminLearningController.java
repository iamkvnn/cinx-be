package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.learning.dto.response.CourseProgressResponse;
import com.cinx.learning.dto.response.CoursesProgressSummaryResponse;
import com.cinx.learning.dto.response.LearningActivityByMonthResponse;
import com.cinx.learning.dto.response.UserLearningSummaryResponse;
import com.cinx.learning.service.activity.ILearningActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLearningController {
    private final ILearningActivityService learningActivityService;

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<ApiResponse<UserLearningSummaryResponse>> getUserSummary(@PathVariable String userId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User learning summary fetched successfully",
                learningActivityService.getUserLearningSummary(userId)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/users/{userId}/course-progress")
    public ResponseEntity<ApiResponse<List<CourseProgressResponse>>> getUserCourseProgress(
            @PathVariable String userId,
            @RequestParam List<String> courseIds
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User course progress fetched successfully",
                learningActivityService.getUserCourseProgress(userId, courseIds)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<ApiResponse<List<LearningActivityByMonthResponse>>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "6") Integer months
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User learning activity fetched successfully",
                learningActivityService.getUserActivityByMonth(userId, months)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/courses/progress-summary")
    public ResponseEntity<ApiResponse<CoursesProgressSummaryResponse>> getCoursesProgressSummary(@RequestParam List<String> courseIds) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Courses progress summary fetched successfully",
                learningActivityService.getCoursesProgressSummary(courseIds)
        ));
    }
}
