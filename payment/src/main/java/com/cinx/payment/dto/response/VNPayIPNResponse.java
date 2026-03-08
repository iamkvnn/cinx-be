package com.cinx.payment.dto.response;

public record VNPayIPNResponse (
        String RspCode,
        String Message
) {
}
