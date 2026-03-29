package com.cinx.course.dto.request;

import lombok.Data;

@Data
public class CreateVideoLessonRequest {
    private String fileKey;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer duration;
}
