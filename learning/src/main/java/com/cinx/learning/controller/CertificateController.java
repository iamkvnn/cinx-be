package com.cinx.learning.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.learning.consts.CertificateStatus;
import com.cinx.learning.dto.response.CertificateRequestResponse;
import com.cinx.learning.service.certificate.ICertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/certificates")
public class CertificateController {
    
    private final ICertificateService certificateService;

    @Operation(summary = "Apply for course certificate", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping("/apply/{courseId}")
    public ResponseEntity<ApiResponse<CertificateRequestResponse>> applyForCertificate(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Requested successfully", certificateService.applyForCertificate(userId, courseId)));
    }

    @Operation(summary = "Get user certificate by course", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/my-certificate/{courseId}")
    public ResponseEntity<ApiResponse<CertificateRequestResponse>> getMyCertificate(@PathVariable String courseId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", certificateService.getCertificate(userId, courseId)));
    }

    @Operation(summary = "Get all user certificates", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/my-certificates")
    public ResponseEntity<ApiResponse<java.util.List<CertificateRequestResponse>>> getMyCertificates() {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", certificateService.getMyCertificates(userId)));
    }

    @Operation(summary = "Get certificate requests by course ID (For Instructor)", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/requests/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<PaginatedApiResponse<CertificateRequestResponse>> getRequestsByCourse(
            @PathVariable String courseId,
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        String currentUserId = AuthenticationUtil.extractUserId();
        Page<CertificateRequestResponse> result = certificateService.getRequestsByCourse(currentUserId, courseId, status, page, size, query, sort);
        return ResponseEntity.ok(new PaginatedApiResponse<>(
                true, "Success",
                result.getContent(),
                new com.cinx.common.dto.PaginatedMetadata(result.getNumber() + 1, size, result.getTotalElements(), result.getTotalPages())
        ));
    }

    @Operation(summary = "Get all certificate requests", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/requests")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<PaginatedApiResponse<CertificateRequestResponse>> getAllRequests(
            @RequestParam(required = false) CertificateStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        Page<CertificateRequestResponse> result = certificateService.getAllRequests(status, page, size, query, sort);
        return ResponseEntity.ok(new PaginatedApiResponse<>(
                true, "Success",
                result.getContent(),
                new com.cinx.common.dto.PaginatedMetadata(result.getNumber() + 1, size, result.getTotalElements(), result.getTotalPages())
        ));
    }

    @Operation(summary = "Approve certificate request (For Instructor)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/requests/{requestId}/approve")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CertificateRequestResponse>> approveCertificate(@PathVariable String requestId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Approved successfully", certificateService.approveCertificate(currentUserId, requestId)));
    }

    @Operation(summary = "Reject certificate request (For Instructor)", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CertificateRequestResponse>> rejectCertificate(@PathVariable String requestId) {
        String currentUserId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Rejected successfully", certificateService.rejectCertificate(currentUserId, requestId)));
    }
}
