package com.cinx.payment.service.payment;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentServiceFactory {
    private final VNPayPaymentService vnPayPaymentService;
    private final MomoPaymentService momoPaymentService;
    private final StripePaymentService stripePaymentService;

    public IPaymentStrategyService getPaymentService(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case VN_PAY -> vnPayPaymentService;
            case MOMO -> momoPaymentService;
            case STRIPE -> stripePaymentService;
        };
    }

    public List<IPaymentStrategyService> getAllPaymentServices() {
        return List.of(momoPaymentService, vnPayPaymentService, stripePaymentService);
    }

    @Transactional
    public Payment updatePaymentMethod(OrderResponse order, PaymentMethod oldPaymentMethod) {
        if (oldPaymentMethod == order.paymentMethod()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "New payment method must be different from current payment method");
        }
        getPaymentService(oldPaymentMethod).deletePayment(order.id());
        return getPaymentService(order.paymentMethod()).createPayment(order);
    }
}
