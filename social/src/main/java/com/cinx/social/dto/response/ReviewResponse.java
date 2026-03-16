package com.cinx.social.dto.response;

import java.util.List;

public record ReviewResponse(
        String id,
        String userId,
        String courseId,
        String content,
        Double rating,
        List<ReviewReportResponse> reports,
        List<ReviewReactionResponse> reactions
) {
}
