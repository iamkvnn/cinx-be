package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.course.dto.request.CreateCourseRequest;
import com.cinx.course.dto.request.UpdateCourseRequest;
import com.cinx.course.dto.response.CourseDetailResponse;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.service.course.ICourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String instructorId
    ) {
        Page<CourseResponse> courses = courseService.getAllCourses(query, categoryId, instructorId, page, size, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseById(@PathVariable("id") String courseId) {
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

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@RequestBody CreateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course created successfully", courseService.createCourse(request))
        );
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable("id") String courseId, @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok().body(
                new ApiResponse<>(true, "Course updated successfully", courseService.updateCourse(courseId, request))
        );
    }

    @Operation(summary = "Internal API to update course rating", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{id}/update-rating")
    public ResponseEntity<ApiResponse<Void>> updateCourseRating(@PathVariable("id") String courseId, @RequestParam Double rating) {
        courseService.updateCourseRating(courseId, rating);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rating updated successfully", null));
    }
}
