package com.cinx.course.dto.request;

import lombok.Data;

@Data
public class UpdateCourseImageRequest {
    private String imageUrl;
    private String s3ObjectKey;
}
