package com.cinx.social.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionDto {
    private String id;
    private String courseId;
    private String lessonId;
    private String userId;
    private String title;
    private String content;
    private Integer upvoteCount;
    private Boolean hasUpvoted;
    private Integer answersCount;
    private LocalDateTime createdAt;
}
