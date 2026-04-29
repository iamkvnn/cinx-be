package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.course.consts.CourseStatus;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.RejectCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseChangeResponse;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.dto.response.RejectCourseResponse;
import com.cinx.course.service.change.ICourseChangeAuditService;
import com.cinx.course.service.course.ICourseService;
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
    private final ICourseChangeAuditService courseChangeAuditService;

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
                page,
                size,
                sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getCourseById(courseId))
        );
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourseById(@RequestParam("ids") List<String> courseIds) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course fetched successfully", courseService.getCourseByIds(courseIds))
        );
    }

    @GetMapping("/{id}/change-history")
    public ResponseEntity<ApiResponse<List<CourseChangeResponse>>> getCourseChangeHistory(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course change history fetched successfully", courseChangeAuditService.getCourseChangeHistory(courseId))
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

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<CourseResponse>> publishCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course published successfully", courseService.publishCourse(courseId))
        );
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<CourseResponse>> approveCourse(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course approved successfully", courseService.approveCourse(courseId))
        );
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<CourseResponse>> rejectCourse(@PathVariable("id") String courseId, @RequestBody RejectCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course rejected successfully", courseService.rejectCourse(courseId, request))
        );
    }

    @GetMapping("/{id}/reject-reason")
    public ResponseEntity<ApiResponse<RejectCourseResponse>> getRejectReason(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Reject reason fetched successfully", courseService.getRejectReason(courseId))
        );
    }
}
