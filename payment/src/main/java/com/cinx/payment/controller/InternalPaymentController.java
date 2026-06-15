package com.cinx.payment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.service.payment.PaymentServiceFactory;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API — called only by other services via Feign (service-to-service).
 * Not exposed externally; blocked at the gateway layer (/internal/** → denyAll).
 */
@Hidden
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentServiceFactory factory;

    @GetMapping
    public ApiResponse<PaymentResponse> getPaymentByOrderId(
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod) {
        return new ApiResponse<>(true, "Payment fetched successfully",
                factory.getPaymentService(paymentMethod).getPaymentByOrderId(orderId));
    }

    @GetMapping("/ids")
    public ApiResponse<List<PaymentResponse>> getPaymentByOrderIds(@RequestParam List<String> orderIds) {
        return new ApiResponse<>(true, "Payments fetched successfully",
                factory.getPaymentService(PaymentMethod.MOMO).getPaymentByIds(orderIds));
    }

    @PutMapping("/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod) {
        return new ApiResponse<>(true, "Payment cancelled successfully",
                factory.getPaymentService(paymentMethod).cancelPayment(null, orderId));
    }
}
