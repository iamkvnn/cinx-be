package com.cinx.payment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.mapper.PaymentMapper;
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
    private final PaymentMapper paymentMapper;

    @PostMapping
    public ApiResponse<PaymentResponse> createPayment(@RequestBody OrderResponse order) {
        return new ApiResponse<>(true, "Payment created successfully",
                paymentMapper.toDto(factory.getPaymentService(order.paymentMethod()).createPayment(order)));
    }

    @PutMapping("/payment-method")
    public ApiResponse<PaymentResponse> updatePaymentMethod(
            @RequestBody OrderResponse order,
            @RequestParam PaymentMethod oldPaymentMethod) {
        return new ApiResponse<>(true, "Payment method updated successfully",
                paymentMapper.toDto(factory.updatePaymentMethod(order, oldPaymentMethod)));
    }

    @GetMapping
    public ApiResponse<PaymentResponse> getPaymentByOrderId(
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod) {
        return new ApiResponse<>(true, "Payment fetched successfully",
                factory.getPaymentService(paymentMethod).getPaymentByOrderId(orderId));
    }

    @GetMapping("/ids")
    public ApiResponse<List<PaymentResponse>> getPaymentByOrderIds(@RequestParam List<String> orderIds) {
        List<PaymentResponse> payments = factory.getAllPaymentServices().stream()
                .flatMap(service -> service.getPaymentByIds(orderIds).stream())
                .toList();
        return new ApiResponse<>(true, "Payments fetched successfully",
                payments);
    }

    @PutMapping("/cancel")
    public ApiResponse<PaymentResponse> cancelPayment(
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod) {
        return new ApiResponse<>(true, "Payment cancelled successfully",
                factory.getPaymentService(paymentMethod).cancelPayment(null, orderId));
    }
}
