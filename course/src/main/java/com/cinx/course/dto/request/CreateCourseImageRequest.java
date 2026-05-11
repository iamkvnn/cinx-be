package com.cinx.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateCourseImageRequest {
    @NotEmpty(message = "Images list cannot be empty")
    @Valid
    private List<ImageDto> images;

    @Data
    public static class ImageDto {
        @NotBlank(message = "File key is required")
        private String fileKey;
    }
}
