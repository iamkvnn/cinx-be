package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.service.image.ICourseImageService;
import com.cinx.course.dto.request.CreateCourseImageRequest;
import com.cinx.course.dto.request.UpdateCourseImageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseImageController {
    private final ICourseImageService courseImageService;

    @PostMapping("/{courseId}/images")
    public ResponseEntity<ApiResponse<?>> uploadCourseImages(@PathVariable String courseId, @RequestBody CreateCourseImageRequest request) {
        courseImageService.saveCourseImages(courseId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course images added successfully", null));
    }

    @PutMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> updateCourseImage(@PathVariable String imageId, @RequestBody UpdateCourseImageRequest request, @PathVariable String courseId) {
        courseImageService.updateCourseImage(imageId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image updated successfully", null));
    }

    @DeleteMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteCourseImage(@PathVariable String imageId, @PathVariable String courseId) {
        courseImageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image deleted successfully", null));
    }
}
