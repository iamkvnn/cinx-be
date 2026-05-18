package com.cinx.course.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record VideoLessonResponse (
        @Schema(example = "https://example.com/videos/12345-spring-boot.mp4")
        String videoUrl,
        @Schema(example = "spring-boot-intro.mp4")
        String fileName,
        @Schema(example = "video/mp4")
        String fileType,
        @Schema(example = "104857600")
        Long fileSize,
        @Schema(example = "600")
        Integer duration,
        @Schema(example = "READY")
        String status,
        @Schema(example = "true")
        Boolean hasQuestions,
        @Schema(example = "5")
        Integer questionCount
){
}
