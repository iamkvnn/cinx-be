package com.cinx.social.service.admin;

import com.cinx.social.dto.response.AdminReportResponse;
import com.cinx.social.model.ReportType;
import org.springframework.data.domain.Page;

public interface IAdminReportService {
    Page<AdminReportResponse> getReports(ReportType type, int page, int size, String query, String sort);
    void dismissReport(String reportId);
    void deleteReportedContent(String reportId);
}
