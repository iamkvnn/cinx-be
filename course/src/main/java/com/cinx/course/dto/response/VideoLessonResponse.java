package com.cinx.course.dto.response;

public record VideoLessonResponse (
        String videoUrl,
        String s3ObjectKey,
        String fileName,
        String fileType,
        Long fileSize,
        Integer duration,
        String status
){
}
