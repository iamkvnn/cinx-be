package com.cinx.payment.service.payment;

import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.model.Payment;

import java.util.List;
import java.util.Map;

public interface IPaymentStrategyService {
    PaymentResponse getPaymentByOrderId(String orderId);
    Payment createPayment(OrderResponse order);
    String getPaymentUrl(String orderId);
    boolean handleCallback(Map<String, String> callbackData);
    Payment validateCallback(Map<String, String> callbackData);
    List<PaymentResponse> getPaymentByIds(List<String> orderIds);
}
