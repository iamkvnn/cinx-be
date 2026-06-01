package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.dto.response.UserEnrollmentSummaryResponse;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollments/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEnrollmentController {
    private final IEnrollmentService enrollmentService;

    @Operation(security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<ApiResponse<UserEnrollmentSummaryResponse>> getUserSummary(@PathVariable String userId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "User enrollment summary fetched successfully",
                enrollmentService.getUserEnrollmentSummary(userId)
        ));
    }
}
