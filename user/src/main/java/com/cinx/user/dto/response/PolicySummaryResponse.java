package com.cinx.user.dto.response;

import com.cinx.user.consts.PolicyStatus;
import com.cinx.user.consts.PolicyType;

import java.time.LocalDateTime;

public record PolicySummaryResponse(
        String id,
        PolicyType policyType,
        String slug,
        String title,
        String summary,
        PolicyStatus status,
        Integer versionNumber,
        LocalDateTime effectiveAt,
        LocalDateTime publishedAt,
        Integer displayOrder
) {
}
