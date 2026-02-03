package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.dto.PaginatedMetadata;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.service.course.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {
    private final ICourseService courseService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getAllCourses(@RequestParam(value = "page", defaultValue = "1") int page,
                                                              @RequestParam(value = "size", defaultValue = "10") int size,
                                                              @RequestParam(value = "query", required = false) String query) {
        Page<CourseResponse> courses = courseService.getAllCourses(page, size, query);
        return ResponseEntity.ok().body(
                new PaginatedApiResponse<>(true, "Courses fetched successfully", courses.getContent(), new PaginatedMetadata(courses.getNumber(), courses.getSize(), courses.getTotalElements(), courses.getTotalPages()))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCourseById(@PathVariable("id") String courseId) {
        return ResponseEntity.ok().body(
                new ApiResponse(true, "Course fetched successfully", courseService.getCourseById(courseId))
        );
    }
}
