package com.cinx.course.dto.request;

import lombok.Data;

@Data
public class UpdateVideoLessonRequest {
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer duration;
}
