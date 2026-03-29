package com.cinx.payment.config;

import com.cinx.payment.dto.request.MomoPaymentRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MomoPaymentConfig {

    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    @Value("${MoMo.secret}")
    private String secretKey;
    @Value("${MoMo.url}")
    private String momoURL;
    @Value("${MoMo.partnerCode}")
    private String partnerCode;
    @Value("${MoMo.accessKey}")
    private String accessKey;

    private final RestTemplate restTemplate;

    public String generateSignature(String data, String secretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public String sendToMomo(MomoPaymentRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", request.partnerCode());
        body.put("accessKey", request.accessKey());
        body.put("requestId", request.requestId());
        body.put("amount", request.amount());
        body.put("orderId", request.orderId());
        body.put("orderInfo", request.orderInfo());
        body.put("redirectUrl", request.redirectUrl());
        body.put("ipnUrl", request.ipnUrl());
        body.put("extraData", request.extraData());
        body.put("requestType", request.requestType().getValue());
        body.put("signature", request.signature());

        return webClient.post()
                .uri(momoURL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .map(RuntimeException::new))
                .bodyToMono(String.class)
                .block();
    }

    public MomoPaymentRequest createPaymentRequest(String orderId, String amount, String orderInfo,
                                                          String returnUrl, String notifyUrl, String extraData, ERequestType requestType, String requestId) throws NoSuchAlgorithmException, InvalidKeyException {
        String requestRawData = "accessKey=" + accessKey + "&" +
                "amount=" + amount + "&" +
                "extraData=" + extraData + "&" +
                "ipnUrl=" + notifyUrl + "&" +
                "orderId=" + orderId + "&" +
                "orderInfo=" + orderInfo + "&" +
                "partnerCode=" + partnerCode + "&" +
                "redirectUrl=" + returnUrl + "&" +
                "requestId=" + requestId + "&" +
                "requestType=" + requestType.getValue();
        String signature = generateSignature(requestRawData, secretKey);
        return new MomoPaymentRequest(partnerCode, accessKey, requestId, amount, orderId, orderInfo, returnUrl,
                notifyUrl, extraData, requestType, signature);
    }

    public boolean isValidSignature(Map<String, String> response) {
        try {
            String signatureFromMoMo = response.get("signature");
            response.remove("signature");
            // Sắp xếp key theo thứ tự alphabet
            StringBuilder rawData = new StringBuilder();
            rawData.append("accessKey").append("=")
                    .append(accessKey)
                    .append("&");
            response.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()) // Sắp xếp key theo thứ tự alphabet
                    .forEach(entry -> {
                        String key = entry.getKey();
                        String value = entry.getValue() != null ? entry.getValue().trim() : "";
                        rawData.append(key).append("=")
                                .append(value)
                                .append("&");
                    });

            if (!rawData.isEmpty()) {
                rawData.setLength(rawData.length() - 1);
            }
            // Tạo signature mới
            String generatedSignature = generateSignature(rawData.toString(), secretKey);
            return generatedSignature.equals(signatureFromMoMo);
        } catch (Exception e) {
            return false;
        }
    }

    @Getter
    public enum ERequestType {
        PAY_WITH_ATM("payWithATM"),
        PAY_WITH_CC("payWithCC");
        private final String value;
        ERequestType(String value) {
            this.value = value;
        }
    }
}
