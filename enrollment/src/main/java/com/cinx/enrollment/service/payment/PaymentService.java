package com.cinx.enrollment.service.payment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.enrollment.config.FeignConfig;
import com.cinx.enrollment.consts.PaymentMethod;
import com.cinx.enrollment.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "payment", path = "/internal/payments", configuration = FeignConfig.class)
public interface PaymentService {
    @GetMapping
    ApiResponse<PaymentResponse> getPaymentByOrderId(@RequestParam String orderId, @RequestParam PaymentMethod paymentMethod);

    @GetMapping("/ids")
    ApiResponse<List<PaymentResponse>> getPaymentByIds(@RequestParam List<String> orderIds);

    @PutMapping("/cancel")
    ApiResponse<PaymentResponse> cancelPayment(@RequestParam String orderId, @RequestParam PaymentMethod paymentMethod);
}
