package com.cinx.course.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateCourseImageRequest {
    private List<ImageDto> images;

    @Data
    public static class ImageDto {
        private String imageUrl;
        private String s3ObjectKey;
    }
}
