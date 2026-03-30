package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.course.service.image.ICourseImageService;
import com.cinx.course.dto.request.CreateCourseImageRequest;
import com.cinx.course.dto.request.UpdateCourseImageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseImageController {
    private final ICourseImageService courseImageService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/{courseId}/images")
    public ResponseEntity<ApiResponse<?>> uploadCourseImages(@PathVariable String courseId, @RequestBody CreateCourseImageRequest request) {
        courseImageService.saveCourseImages(courseId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course images added successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> updateCourseImage(@PathVariable String imageId, @RequestBody UpdateCourseImageRequest request, @PathVariable String courseId) {
        courseImageService.updateCourseImage(imageId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image updated successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ApiResponse<?>> deleteCourseImage(@PathVariable String imageId, @PathVariable String courseId) {
        courseImageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Course image deleted successfully", null));
    }
}
