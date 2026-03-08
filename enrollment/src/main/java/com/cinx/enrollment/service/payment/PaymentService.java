package com.cinx.enrollment.service.payment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.consts.PaymentMethod;
import com.cinx.enrollment.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "payment", path = "/api/v1/payments")
public interface PaymentService {
    @GetMapping
    ApiResponse<PaymentResponse> getPaymentByOrderId(@RequestParam String orderId, @RequestParam PaymentMethod paymentMethod);
}
