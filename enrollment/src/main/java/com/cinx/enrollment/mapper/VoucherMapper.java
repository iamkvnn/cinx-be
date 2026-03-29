package com.cinx.enrollment.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.common.mapper.CreateMapper;
import com.cinx.common.mapper.UpdateMapper;
import com.cinx.enrollment.dto.request.CreateVoucherRequest;
import com.cinx.enrollment.dto.request.UpdateVoucherRequest;
import com.cinx.enrollment.dto.response.VoucherResponse;
import com.cinx.enrollment.model.Voucher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapper extends
        BaseMapper<Voucher, VoucherResponse>,
        CreateMapper<Voucher, CreateVoucherRequest>,
        UpdateMapper<Voucher, UpdateVoucherRequest> {
}
