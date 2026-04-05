package com.cinx.enrollment.service.voucher;

import com.cinx.enrollment.dto.request.CreateVoucherRequest;
import com.cinx.enrollment.dto.request.UpdateVoucherRequest;
import com.cinx.enrollment.dto.response.VoucherResponse;
import org.springframework.data.domain.Page;

public interface IVoucherService {
    Page<VoucherResponse> getVouchers(int page, int size, String query, String sort);
    VoucherResponse getVoucherById(String id);
    VoucherResponse getVoucherByCode(String code);
    void createVoucher(CreateVoucherRequest request);
    void updateVoucher(String id, UpdateVoucherRequest request);
    void deleteVoucher(String id);
    VoucherResponse validateVoucher(String code, Long amount);
}
