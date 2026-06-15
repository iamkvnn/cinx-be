package com.cinx.payment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.dto.request.PaymentRequest;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.dto.response.VNPayIPNResponse;
import com.cinx.payment.service.payment.PaymentServiceFactory;
import com.cinx.payment.service.payment.adapter.VNPayCallbackAdapter;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentServiceFactory factory;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<String>> requestMomoPayment(@Valid @RequestBody PaymentRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        String response = factory.getPaymentService(request.paymentMethod()).getPaymentUrl(userId, request.orderId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", response));
    }

    @PostMapping("/momo-callback")
    public ResponseEntity<ApiResponse<Void>> handleMoMoCallback(@RequestBody Map<String, String> response) {
        System.out.println("MoMo callback received: " + response);
        factory.getPaymentService(PaymentMethod.MOMO).handleCallback(response);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/IPN")
    public ResponseEntity<VNPayIPNResponse> handleVNPayIPN(HttpServletRequest request) {
        if (factory.getPaymentService(PaymentMethod.VN_PAY).handleCallback(new VNPayCallbackAdapter(request))) {
            return ResponseEntity.ok(new VNPayIPNResponse("00", "Success"));
        } else {
            return ResponseEntity.ok(new VNPayIPNResponse("99", "Failed"));
        }
    }

    @PutMapping("/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@RequestParam String orderId, @RequestParam PaymentMethod paymentMethod) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment cancelled successfully", factory.getPaymentService(paymentMethod).cancelPayment(userId, orderId)));
    }
}
