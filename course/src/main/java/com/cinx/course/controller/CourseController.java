package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.dto.PaginatedMetadata;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.course.dto.response.CourseResponse;
import com.cinx.course.service.course.ICourseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            @RequestParam(required = false) String sort
    ) throws JsonProcessingException {
        int pageIndex = Math.max(page - 1, 0);
        Sort s = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> sortMap = mapper.readValue(sort, new TypeReference<>() {});
            s = sortMap.entrySet().stream()
                    .map(e -> new Sort.Order(Sort.Direction.fromString(e.getValue()), e.getKey()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Sort::by));
        }

        Pageable pageable = PageRequest.of(pageIndex, size, s);
        Page<CourseResponse> courses = courseService.getAllCourses(query, pageable);
        return ResponseEntity.ok().body(
                PaginationWrapper.wrap(courses)
        );
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
}
