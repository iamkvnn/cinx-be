package com.cinx.learning.dto.response;

import com.cinx.learning.consts.CertificateStatus;
import java.time.LocalDateTime;

public record CertificateRequestResponse(
        String id,
        String userId,
        String courseId,
        CertificateStatus status,
        String certificateUrl,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt
) {
}