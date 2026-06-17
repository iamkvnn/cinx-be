package com.cinx.payment.service.payment.adapter;

import java.util.HashMap;

public class StripeCallbackAdapter extends HashMap<String, String> {
    public static final String PAYLOAD = "payload";
    public static final String SIGNATURE_HEADER = "signatureHeader";

    public StripeCallbackAdapter(String payload, String signatureHeader) {
        put(PAYLOAD, payload);
        put(SIGNATURE_HEADER, signatureHeader);
    }
}
