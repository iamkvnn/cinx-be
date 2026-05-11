package com.cinx.social.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewReplyDto {
    private String id;
    private String reviewId;
    private String instructorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
