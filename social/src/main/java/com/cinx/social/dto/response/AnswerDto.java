package com.cinx.social.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnswerDto {
    private String id;
    private String questionId;
    private String parentAnswerId;
    private String userId;
    private String content;
    private Boolean isInstructorAnswer;
    private Integer upvoteCount;
    private Boolean hasUpvoted;
    private Integer depth;
    private Integer repliesCount;
    private LocalDateTime createdAt;
}
