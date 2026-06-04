package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.course.consts.CoursePublishStatus;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.response.CourseCurriculumResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.service.course.ICourseService;
import com.cinx.course.service.curriculum.ICurriculumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {
    private final ICourseService courseService;
    private final ICurriculumService curriculumService;

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer priceFrom,
            @RequestParam(required = false) Integer priceTo,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) CoursePublishStatus publishStatus,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String instructorId
    ) {
        Page<CourseResponse> courses = courseService.getAllCourses(
                query,
                categoryId,
                instructorId,
                rating,
                priceFrom,
                priceTo,
                status,
                publishStatus,
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Course fetched successfully", courseService.getCourseById(courseId)));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDraft(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft fetched successfully",
                courseService.getDraftCourseById(courseId)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getCourseCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum fetched successfully",
                curriculumService.getPublishedSnapshotCurriculum(courseId)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getDraftCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft curriculum fetched successfully",
                curriculumService.getDraftCurriculum(courseId)
        ));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CourseResponse>> approveCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Course approved successfully", courseService.approveCourse(courseId)));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CourseResponse>> rejectCourse(
            @PathVariable("id") String courseId,
            @RequestBody RejectCourseRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Course rejected successfully", courseService.rejectCourse(courseId, request)));
    }

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/recommendation/replay")
    public ResponseEntity<ApiResponse<Void>> replayRecommendationEvents() {
        courseService.replayRecommendationEvents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Course recommendation replay enqueued successfully", null));
    }
}
