package com.cinx.course.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PresignedUrlResponse;
import com.cinx.course.service.s3.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/upload")
public class PresignedUrlController {

    private final S3Service s3Service;

    public PresignedUrlController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType) {
        
        // Generate a unique object key for course videos / files
        String fileKey = "courses/" + UUID.randomUUID() + "-" + fileName;
        String presignedUrl = s3Service.generatePresignedUrl(fileKey, contentType);

        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .fileKey(fileKey)
                .presignedUrl(presignedUrl)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "Success", response));
    }
}