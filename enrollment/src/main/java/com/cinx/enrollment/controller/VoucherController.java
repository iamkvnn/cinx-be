package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiQuery;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.enrollment.dto.request.CreateVoucherRequest;
import com.cinx.enrollment.dto.request.UpdateVoucherRequest;
import com.cinx.enrollment.dto.response.VoucherResponse;
import com.cinx.enrollment.service.voucher.IVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final IVoucherService voucherService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<VoucherResponse>> getVouchers(@Valid @ModelAttribute PaginatedApiQuery query) {
        return ResponseEntity.ok(PaginationWrapper.wrap(voucherService.getVouchers(query)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable String id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", voucherService.getVoucherById(id)));
    }

    @GetMapping("/code")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCode(@RequestParam String code) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", voucherService.getVoucherByCode(code)));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        voucherService.createVoucher(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher created successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateVoucher(@PathVariable String id, @Valid @RequestBody UpdateVoucherRequest request) {
        voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher updated successfully", null));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteVoucher(@PathVariable String id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Voucher deleted successfully", null));
    }
}
