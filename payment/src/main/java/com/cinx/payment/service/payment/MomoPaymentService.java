package com.cinx.payment.service.payment;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.payment.config.MomoPaymentConfig;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.consts.PaymentStatus;
import com.cinx.payment.dto.request.MomoPaymentRequest;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.mapper.PaymentMapper;
import com.cinx.payment.model.MomoPayment;
import com.cinx.payment.model.Payment;
import com.cinx.payment.repository.MomoPaymentRepository;
import com.cinx.payment.service.enrollment.EnrollmentService;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MomoPaymentService extends PaymentTemplate {
    private final MomoPaymentRepository momoPaymentRepository;
    private final MomoPaymentConfig momoConfig;
    private final EnrollmentService enrollmentService;
    private final PaymentMapper paymentMapper;

    @Value("${FE_BASE_URL}")
    private String feBaseUrl;

    public MomoPaymentService(MomoPaymentRepository momoPaymentRepository, MomoPaymentConfig momoConfig, EnrollmentService enrollmentService, PaymentMapper paymentMapper) {
        this.momoPaymentRepository = momoPaymentRepository;
        this.momoConfig = momoConfig;
        this.enrollmentService = enrollmentService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return momoPaymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
    }

    @Override
    public List<PaymentResponse> getPaymentByIds(List<String> orderIds) {
        return momoPaymentRepository.findAllByOrderIds(orderIds).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public MomoPayment createPayment(OrderResponse order) {
        MomoPayment payment = MomoPayment.builder()
                .orderId(order.id())
                .requestId("REQ" + order.id())
                .amount(order.totalPrice() - order.discounted())
                .status(PaymentStatus.PROCESSING)
                .paymentMessage("Thanh toán đơn hàng " + order.id())
                .build();
        return momoPaymentRepository.save(payment);
    }

    @Override
    public String getPaymentUrl(String orderId) {
        String userId = AuthenticationUtil.extractUserId();
        OrderResponse order = enrollmentService.getOrderById(orderId).data();
        MomoPayment momoPayment = momoPaymentRepository
                .findByOrderId(orderId)
                .orElseGet(() -> createPayment(order));
        if (momoPayment.getPaymentUrl() != null && momoPayment.getUrlExpireTime().isAfter(LocalDateTime.now())) {
            return momoPayment.getPaymentUrl();
        }
        String returnUrl = "exp://10.100.100.227:8083/--/payment-success";
        String notifyUrl = "https://34bf-27-65-59-126.ngrok-free.app/api/v1/payments/momo-callback";
        MomoPaymentRequest request;
        try {
            request = momoConfig.createPaymentRequest(orderId, momoPayment.getAmount().toString(),
                        momoPayment.getPaymentMessage(), returnUrl, notifyUrl, "", MomoPaymentConfig.ERequestType.PAY_WITH_CC, momoPayment.getRequestId());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        String response = momoConfig.sendToMomo(request);
        System.out.println("Momo response: " + response);
        if (response == null) {
            throw new RuntimeException("Failed to get payment URL");
        }
        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
        String url = jsonResponse.get("payUrl").getAsString();
        momoPayment.setPaymentUrl(url);
        momoPayment.setUrlExpireTime(LocalDateTime.now().plusHours(16).plusMinutes(50));
        momoPaymentRepository.save(momoPayment);
        return url;
    }

    @Override
    protected void updatePayment(Payment payment) {
        momoPaymentRepository.save((MomoPayment) payment);
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        if(Objects.equals(callbackData.get("resultCode"), "0") && momoConfig.isValidSignature(callbackData)){
            String orderId = callbackData.get("orderId");
            MomoPayment payment = momoPaymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new BadRequestException("Payment not found for orderId: " + orderId));
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaymentDate(LocalDateTime.now());
            return payment;
        }
        return null;
    }
}
