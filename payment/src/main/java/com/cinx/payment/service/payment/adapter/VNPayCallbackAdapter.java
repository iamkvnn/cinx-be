package com.cinx.payment.service.payment.adapter;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayCallbackAdapter extends HashMap<String, String> {

    public VNPayCallbackAdapter(HttpServletRequest request) {
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName;
            String fieldValue;
            fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                put(fieldName, fieldValue);
            }
        }
    }
}
