package com.cinx.payment.service.payment;

import com.cinx.payment.config.VNPayPaymentConfig;
import com.cinx.payment.consts.PaymentStatus;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.mapper.PaymentMapper;
import com.cinx.payment.messaging.PaymentEventProducer;
import com.cinx.payment.model.Payment;
import com.cinx.payment.model.VNPayPayment;
import com.cinx.payment.repository.VNPayPaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayPaymentService extends PaymentTemplate {
    private final VNPayPaymentRepository vnPayPaymentRepository;
    private final VNPayPaymentConfig vnPayConfig;
    private final PaymentMapper paymentMapper;

    @Value("${VNPay.vnp_Version}")
    private String vnp_Version;
    @Value("${VNPay.vnp_Command}")
    private String vnp_Command;
    @Value("${VNPay.vnp_TmnCode}")
    private String vnp_TmnCode;
    @Value("${VNPay.vnp_Url}")
    private String vnp_Url;
    @Value("${FE_BASE_URL}")
    private String feBaseUrl;

    public VNPayPaymentService(VNPayPaymentRepository vnPayPaymentRepository, VNPayPaymentConfig vnPayConfig, PaymentMapper paymentMapper, PaymentEventProducer paymentEventProducer) {
        super(paymentEventProducer);
        this.vnPayPaymentRepository = vnPayPaymentRepository;
        this.vnPayConfig = vnPayConfig;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return vnPayPaymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId: " + orderId));
    }

    @Override
    public List<PaymentResponse> getPaymentByIds(List<String> paymentIds) {
        return vnPayPaymentRepository.findAllById(paymentIds).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public VNPayPayment createPayment(OrderResponse order) {
        VNPayPayment payment = VNPayPayment.builder()
                .orderId(order.id())
                .amount(order.totalPrice() - order.discounted())
                .paymentMessage("Thanh toan đon hang " + order.id())
                .status(PaymentStatus.PROCESSING)
                .build();
        return vnPayPaymentRepository.save(payment);
    }

    public String getPaymentUrl(String orderId) {
        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        long vnp_Amount = 100000 * 100;
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnp_Version);
        params.put("vnp_Command", vnp_Command);
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(vnp_Amount));
        params.put("vnp_BankCode", "VNBANK");
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", "103.149.252.125");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        String orderType = "other";
        params.put("vnp_OrderType", orderType);
        params.put("vnp_ReturnUrl", feBaseUrl + "/checkouts/thank-you/" + orderId);
        params.put("vnp_ExpireDate", expireDate);
        params.put("vnp_TxnRef", vnp_TxnRef);
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash;
        try {
            vnp_SecureHash = vnPayConfig.hmacSHA512(hashData.toString());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return vnp_Url + "?" + queryUrl;
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        String vnp_SecureHash = callbackData.get("vnp_SecureHash");
        callbackData.remove("vnp_SecureHashType");
        callbackData.remove("vnp_SecureHash");
        try {
            String signValue = vnPayConfig.hashAllFields(callbackData);
            if (signValue.equals(vnp_SecureHash) && callbackData.get("vnp_ResponseCode").equals("00")) {

            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void updatePayment(Payment payment) {
        vnPayPaymentRepository.save((VNPayPayment) payment);
    }
}
