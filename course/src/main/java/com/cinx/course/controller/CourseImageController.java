package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.service.image.ICourseImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseImageController {
    private final ICourseImageService courseImageService;

    @PostMapping("/{courseId}/images")
    public ResponseEntity<ApiResponse<?>> uploadCourseImages(@PathVariable String courseId, List<MultipartFile> files) {
        courseImageService.saveCourseImages(courseId, files);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course images uploaded successfully", null));
    }

    @PutMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> updateCourseImage(@PathVariable String imageId, MultipartFile file, @PathVariable String courseId) {
        courseImageService.updateCourseImages(imageId, file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image updated successfully", null));
    }

    @DeleteMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteCourseImage(@PathVariable String imageId, @PathVariable String courseId) {
        courseImageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image deleted successfully", null));
    }
}
