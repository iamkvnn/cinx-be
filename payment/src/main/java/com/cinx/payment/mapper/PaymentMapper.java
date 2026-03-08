package com.cinx.payment.mapper;

import com.cinx.common.mapper.BaseMapper;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper extends BaseMapper<Payment, PaymentResponse> {
}
