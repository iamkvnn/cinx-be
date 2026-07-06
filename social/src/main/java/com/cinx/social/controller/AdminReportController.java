package com.cinx.social.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.social.dto.response.AdminReportResponse;
import com.cinx.social.model.ReportType;
import com.cinx.social.service.admin.IAdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final IAdminReportService adminReportService;

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<PaginatedApiResponse<AdminReportResponse>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(PaginationWrapper.wrap(adminReportService.getReports(type, page, size, sort)));
    }

    @DeleteMapping("/{reportId}/dismiss")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> dismissReport(@PathVariable String reportId) {
        adminReportService.dismissReport(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Report dismissed", null));
    }

    @DeleteMapping("/{reportId}/content")
    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<Void>> deleteReportedContent(@PathVariable String reportId) {
        adminReportService.deleteReportedContent(reportId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Reported content deleted", null));
    }
}
