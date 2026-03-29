package com.cinx.payment.dto.request;

import com.cinx.payment.config.MomoPaymentConfig;

public record MomoPaymentRequest (
    String partnerCode,
    String accessKey,
    String requestId,
    String amount,
    String orderId,
    String orderInfo,
    String redirectUrl,
    String ipnUrl,
    String extraData,
    MomoPaymentConfig.ERequestType requestType,
    String signature
) {
}