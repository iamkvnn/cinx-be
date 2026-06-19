package com.cinx.payment.service.payment;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.payment.config.VNPayPaymentConfig;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.consts.PaymentStatus;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.mapper.PaymentMapper;
import com.cinx.payment.messaging.PaymentEventProducer;
import com.cinx.payment.model.Payment;
import com.cinx.payment.model.VNPayPayment;
import com.cinx.payment.repository.VNPayPaymentRepository;
import com.cinx.payment.service.enrollment.EnrollmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayPaymentService extends PaymentTemplate {
    private final VNPayPaymentRepository vnPayPaymentRepository;
    private final VNPayPaymentConfig vnPayConfig;
    private final PaymentMapper paymentMapper;
    private final EnrollmentService enrollmentService;

    @Value("${VNPay.vnp_Version}")
    private String vnp_Version;
    @Value("${VNPay.vnp_Command}")
    private String vnp_Command;
    @Value("${VNPay.vnp_TmnCode}")
    private String vnp_TmnCode;
    @Value("${VNPay.vnp_Url}")
    private String vnp_Url;
    @Value("${feBaseUrl}")
    private String feBaseUrl;

    public VNPayPaymentService(VNPayPaymentRepository vnPayPaymentRepository, VNPayPaymentConfig vnPayConfig, PaymentMapper paymentMapper, EnrollmentService enrollmentService, PaymentEventProducer paymentEventProducer) {
        super(paymentEventProducer);
        this.vnPayPaymentRepository = vnPayPaymentRepository;
        this.vnPayConfig = vnPayConfig;
        this.paymentMapper = paymentMapper;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return vnPayPaymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
    }

    @Override
    public List<PaymentResponse> getPaymentByIds(List<String> orderIds) {
        return vnPayPaymentRepository.findAllByOrderIds(orderIds).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public VNPayPayment createPayment(OrderResponse order) {
        Optional<VNPayPayment> existingPayment = vnPayPaymentRepository.findByOrderId(order.id());
        if (existingPayment.isPresent()) {
            throw new AlreadyExistException("Payment already exists for orderId: " + order.id());
        }
        VNPayPayment payment = VNPayPayment.builder()
                .orderId(order.id())
                .amount(order.totalPrice() - order.discounted())
                .paymentMessage("Thanh toan đon hang " + order.id())
                .status(PaymentStatus.PROCESSING)
                .build();
        return vnPayPaymentRepository.save(payment);
    }

    @Override
    public void deletePayment(String orderId) {
        VNPayPayment payment = vnPayPaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot delete a paid payment");
        }
        vnPayPaymentRepository.delete(payment);
    }

    @Override
    public String getCheckoutLink(String userId, String paymentId) {
        VNPayPayment payment = vnPayPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found for id: " + paymentId));
        OrderResponse order = enrollmentService.getOrderById(payment.getOrderId()).data();
        ensureOrderPayableByCurrentUser(order, userId);
        if (payment.getPaymentUrl() != null
                && payment.getUrlExpireTime() != null
                && payment.getUrlExpireTime().isAfter(LocalDateTime.now())) {
            return payment.getPaymentUrl();
        }
        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        long vnp_Amount = payment.getAmount() * 100;
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
        params.put("vnp_OrderInfo", "Thanh toan don hang " + payment.getOrderId());
        String orderType = "other";
        params.put("vnp_OrderType", orderType);
        params.put("vnp_ReturnUrl", feBaseUrl + "/checkouts/thank-you/" + payment.getOrderId());
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
        String paymentUrl = vnp_Url + "?" + queryUrl;
        payment.setPaymentUrl(paymentUrl);
        payment.setUrlExpireTime(LocalDateTime.now().plusMinutes(15));
        vnPayPaymentRepository.save(payment);
        return paymentUrl;
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        String vnp_SecureHash = callbackData.get("vnp_SecureHash");
        callbackData.remove("vnp_SecureHashType");
        callbackData.remove("vnp_SecureHash");
        try {
            String signValue = vnPayConfig.hashAllFields(callbackData);
            if (signValue.equals(vnp_SecureHash) && callbackData.get("vnp_ResponseCode").equals("00")) {
                String orderId = extractOrderId(callbackData.get("vnp_OrderInfo"));
                VNPayPayment payment = vnPayPaymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setTransactionNumber(callbackData.get("vnp_TransactionNo"));
                payment.setBank(callbackData.get("vnp_BankCode"));
                payment.setVnPayResponseCode(callbackData.get("vnp_ResponseCode"));
                return payment;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private String extractOrderId(String orderInfo) {
        String prefix = "Thanh toan don hang ";
        if (orderInfo == null || !orderInfo.startsWith(prefix)) {
            throw new RuntimeException("Invalid VNPay order info");
        }
        return orderInfo.substring(prefix.length());
    }

    @Override
    protected void updatePayment(Payment payment) {
        vnPayPaymentRepository.save((VNPayPayment) payment);
    }

    @Override
    public PaymentResponse cancelPayment(String userId, String orderId) {
            VNPayPayment payment = vnPayPaymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
            if (payment.getStatus() == PaymentStatus.PAID) {
                throw new BadRequestException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot cancel a paid payment");
            }
            payment.setStatus(PaymentStatus.CANCELLED);
            return paymentMapper.toDto(vnPayPaymentRepository.save(payment));
    }

    private void ensureOrderPayableByCurrentUser(OrderResponse order, String userId) {
        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        if (!Objects.equals(order.userId(), userId)) {
            throw new BadRequestException(ErrorCode.NOT_RESOURCE_OWNER, "You are not allowed to pay this order");
        }
        if (order.paymentMethod() != PaymentMethod.VN_PAY) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Order is not configured for VN_PAY payment");
        }
    }
}
