package com.cinx.enrollment.service.voucher;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.enrollment.dto.request.CreateVoucherRequest;
import com.cinx.enrollment.dto.request.UpdateVoucherRequest;
import com.cinx.enrollment.dto.response.VoucherResponse;
import com.cinx.enrollment.mapper.VoucherMapper;
import com.cinx.enrollment.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherService implements IVoucherService{
    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public Page<VoucherResponse> getVouchers(int page, int size, String query, String sort) {
        return voucherRepository.findAll(query, PageRequest.of(page - 1, size, SortConverter.toSort(sort))).map(voucherMapper::toDto);
    }

    @Override
    public VoucherResponse getVoucherById(String id) {
        return voucherRepository.findById(id)
                .map(voucherMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Voucher not found with id: " + id));
    }

    @Override
    public VoucherResponse getVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
                .map(voucherMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Voucher not found with code: " + code));
    }

    @Override
    public void createVoucher(CreateVoucherRequest request) {
        voucherRepository.save(voucherMapper.toModel(request));
    }

    @Override
    public void updateVoucher(String id, UpdateVoucherRequest request) {
            voucherRepository.findById(id)
                    .ifPresentOrElse(existingVoucher -> {
                        voucherMapper.partialUpdate(existingVoucher, request);
                        voucherRepository.save(existingVoucher);
                    },
                    () -> {
                        throw new NotFoundException("Voucher not found with id: " + id);
                    });
    }

    @Override
    public void deleteVoucher(String id) {
        if (!voucherRepository.existsById(id)) {
            throw new NotFoundException("Voucher not found with id: " + id);
        }
        voucherRepository.deleteById(id);
    }

    @Override
    public VoucherResponse validateVoucher(String code, Long amount) {
        VoucherResponse voucher = getVoucherByCode(code);
        if (voucher == null) {
            throw new BadRequestException(ErrorCode.VOUCHER_INVALID, "Invalid voucher code");
        }
        if (voucher.validTo().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorCode.VOUCHER_EXPIRED, "Voucher has expired");
        }
        if (voucher.validFrom().isAfter(LocalDateTime.now())) {
            throw new BadRequestException(ErrorCode.VOUCHER_NOT_ACTIVE, "Voucher is not valid yet");
        }
        if (voucher.minPurchaseAmount() != null && amount < voucher.minPurchaseAmount()) {
            throw new BadRequestException(ErrorCode.VOUCHER_MIN_PURCHASE_NOT_MET, "Minimum purchase amount for this voucher is " + voucher.minPurchaseAmount());
        }
        if (voucher.quantity() != null && voucher.quantity() <= 0) {
            throw new BadRequestException(ErrorCode.VOUCHER_OUT_OF_STOCK, "Voucher is out of stock");
        }
        return voucher;
    }
}
