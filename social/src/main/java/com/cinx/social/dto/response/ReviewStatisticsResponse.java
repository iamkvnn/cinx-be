package com.cinx.social.dto.response;

import java.util.Map;

public record ReviewStatisticsResponse(
        Long reviewCount,
        Double averageRating,
        Map<String, Long> ratingDistribution,
        Double replyRate
) {
}
