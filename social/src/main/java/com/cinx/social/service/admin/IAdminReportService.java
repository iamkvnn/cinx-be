package com.cinx.social.service.admin;

import com.cinx.social.model.Report;
import com.cinx.social.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAdminReportService {
    Page<Report> getReports(ReportType type, int page, int size, String sort);
    void dismissReport(String reportId);
    void deleteReportedContent(String reportId);
}
