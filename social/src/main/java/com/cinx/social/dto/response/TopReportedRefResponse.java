package com.cinx.social.dto.response;

import com.cinx.social.model.ReportType;

public record TopReportedRefResponse(
        String refId,
        ReportType type,
        Long reportCount
) {
}
