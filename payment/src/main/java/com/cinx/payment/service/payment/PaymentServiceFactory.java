package com.cinx.payment.service.payment;

import com.cinx.payment.consts.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentServiceFactory {
    private final VNPayPaymentService vnPayPaymentService;
    private final MomoPaymentService momoPaymentService;

    public IPaymentStrategyService getPaymentService(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case VN_PAY -> vnPayPaymentService;
            case MOMO -> momoPaymentService;
        };
    }
}
