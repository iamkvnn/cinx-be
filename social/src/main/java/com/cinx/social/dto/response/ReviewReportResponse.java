package com.cinx.social.dto.response;

public record ReviewReportResponse(
        String id,
        String reviewId,
        String reporterId,
        String reason
) {
}
