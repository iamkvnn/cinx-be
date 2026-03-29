package com.cinx.payment.service.payment;

import com.cinx.payment.messaging.PaymentEventProducer;
import com.cinx.payment.messaging.event.PaymentEvent;
import com.cinx.payment.model.Payment;
import lombok.RequiredArgsConstructor;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public abstract class PaymentTemplate implements IPaymentStrategyService{
    private final PaymentEventProducer paymentEventProducer;

    @Override
    public boolean handleCallback(Map<String, String> callbackData){
        Payment payment = validateCallback(callbackData);
        if (payment == null) {
            return false;
        }
        String orderId = callbackData.get("orderId");
        if (orderId == null) {
            orderId = callbackData.get("vnp_OrderInfo").substring(20);
        }
        updatePayment(payment);
        paymentEventProducer.publishPaymentSuccessEvent(new PaymentEvent(payment.getOrderId(), payment.getStatus()));
        return true;
    }

    protected abstract void updatePayment(Payment payment);

    protected void sendNotification() {
        Locale localeVN = Locale.forLanguageTag("vi-VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(localeVN);
        currencyFormatter.setCurrency(Currency.getInstance("VND"));
    }
}
