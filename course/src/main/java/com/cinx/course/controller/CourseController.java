package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.ReorderLessonsRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.*;
import com.cinx.course.service.course.ICourseService;
import com.cinx.course.service.curriculum.ICurriculumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {
    private final ICourseService courseService;
    private final ICurriculumService curriculumService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer priceFrom,
            @RequestParam(required = false) Integer priceTo,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String instructorId
    ) {
        Page<CourseResponse> courses = courseService.getAllPublishedCourses(
                query,
                categoryId,
                instructorId,
                rating,
                priceFrom,
                priceTo,
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getPublishedCourseById(courseId))
        );
    }

    @Operation(summary = "Get my courses", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/mine")
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getMyCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer priceFrom,
            @RequestParam(required = false) Integer priceTo,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String categoryId
    ) {
        Page<CourseResponse> courses = courseService.getAllCourses(
                query,
                categoryId,
                AuthenticationUtil.extractUserId(),
                rating,
                priceFrom,
                priceTo,
                status,
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @Operation(summary = "Get editable course draft", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseDraft(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft fetched successfully",
                courseService.getCourseById(courseId)
        ));
    }

    @GetMapping("/{id}/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getPublishedCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum fetched successfully",
                curriculumService.getPublishedCurriculum(courseId)
        ));
    }

    @Operation(summary = "Get editable course curriculum", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{id}/draft/curriculum")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> getDraftCurriculum(@PathVariable("id") String courseId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course draft curriculum fetched successfully",
                curriculumService.getDraftCurriculum(courseId)
        ));
    }

    @Operation(summary = "Reorder editable course curriculum", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}/curriculum/reorder")
    public ResponseEntity<ApiResponse<CourseCurriculumResponse>> reorderCurriculum(
            @PathVariable("id") String courseId,
            @Valid @RequestBody ReorderLessonsRequest request
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Course curriculum reordered successfully",
                curriculumService.reorderCurriculum(courseId, request)
        ));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourseById(@RequestParam("ids") List<String> courseIds) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getPublishedCourseByIds(courseIds))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course created successfully", courseService.createCourse(request))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable("id") String courseId, @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course updated successfully", courseService.updateCourse(courseId, request))
        );
    }

    @Operation(summary = "Submit course for approval", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<CourseResponse>> submitCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course submitted successfully", courseService.submitCourse(courseId))
        );
    }

    @GetMapping("/{id}/reject-reason")
    public ResponseEntity<ApiResponse<RejectCourseResponse>> getRejectReason(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Reject reason fetched successfully", courseService.getRejectReason(courseId))
        );
    }
}
