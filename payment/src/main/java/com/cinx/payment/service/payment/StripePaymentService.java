package com.cinx.payment.service.payment;

import com.cinx.common.exception.AlreadyExistException;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.consts.PaymentStatus;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.dto.response.PaymentResponse;
import com.cinx.payment.mapper.PaymentMapper;
import com.cinx.payment.messaging.PaymentEventProducer;
import com.cinx.payment.model.Payment;
import com.cinx.payment.model.StripePayment;
import com.cinx.payment.repository.StripePaymentRepository;
import com.cinx.payment.service.enrollment.EnrollmentService;
import com.cinx.payment.service.payment.adapter.StripeCallbackAdapter;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StripePaymentService extends PaymentTemplate {
    private final StripePaymentRepository stripePaymentRepository;
    private final EnrollmentService enrollmentService;
    private final PaymentMapper paymentMapper;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Getter
    @Value("${stripe.currency}")
    private String currency;

    public StripePaymentService(StripePaymentRepository stripePaymentRepository,
                                EnrollmentService enrollmentService,
                                PaymentMapper paymentMapper,
                                PaymentEventProducer paymentEventProducer) {
        super(paymentEventProducer);
        this.stripePaymentRepository = stripePaymentRepository;
        this.enrollmentService = enrollmentService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return stripePaymentRepository.findByOrderId(orderId)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
    }

    @Override
    public List<PaymentResponse> getPaymentByIds(List<String> orderIds) {
        return stripePaymentRepository.findAllByOrderIds(orderIds).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public StripePayment createPayment(OrderResponse order) {
        if (stripePaymentRepository.findByOrderId(order.id()).isPresent()) {
            throw new AlreadyExistException("Payment already exists for orderId: " + order.id());
        }
        return stripePaymentRepository.save(newPayment(order));
    }

    private StripePayment newPayment(OrderResponse order) {
        return StripePayment.builder()
                .orderId(order.id())
                .amount(order.totalPrice() - order.discounted())
                .paymentMessage("Thanh toan don hang " + order.id())
                .status(PaymentStatus.PROCESSING)
                .build();
    }

    @Override
    public void deletePayment(String orderId) {
        StripePayment payment = stripePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot delete a paid payment");
        }
        stripePaymentRepository.delete(payment);
    }

    @Override
    public String getCheckoutLink(String userId, String paymentId) {
        StripePayment payment = stripePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found for id: " + paymentId));
        OrderResponse order = enrollmentService.getOrderById(payment.getOrderId()).data();
        ensureOrderPayableByCurrentUser(order, userId);

        if (payment.getPaymentUrl() != null
                && payment.getUrlExpireTime() != null
                && payment.getUrlExpireTime().isAfter(LocalDateTime.now())) {
            return payment.getPaymentUrl();
        }

        Session session = createCheckoutSession(order, payment);
        payment.setCheckoutSessionId(session.getId());
        payment.setPaymentUrl(session.getUrl());
        if (session.getExpiresAt() != null) {
            payment.setUrlExpireTime(LocalDateTime.ofEpochSecond(session.getExpiresAt(), 0, ZoneOffset.UTC));
        }
        stripePaymentRepository.save(payment);
        return session.getUrl();
    }

    @Override
    public Payment validateCallback(Map<String, String> callbackData) {
        Event event;
        String payload = callbackData.get(StripeCallbackAdapter.PAYLOAD);
        String signatureHeader = callbackData.get(StripeCallbackAdapter.SIGNATURE_HEADER);
        try {
            event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return null;
        }
        if (stripePaymentRepository.existsByStripeEventId(event.getId())) {
            return null;
        }
        if (!"checkout.session.completed".equals(event.getType())) {
            return null;
        }

        Object dataObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(dataObject instanceof Session session)) {
            return null;
        }

        String orderId = session.getMetadata() != null ? session.getMetadata().get("orderId") : null;
        if (orderId == null) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Stripe session does not contain orderId");
        }

        StripePayment payment = stripePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BadRequestException("Payment not found for orderId: " + orderId));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentIntentId(session.getPaymentIntent());
        payment.setStripePaymentStatus(session.getPaymentStatus());
        payment.setStripeEventId(event.getId());
        return payment;
    }

    @Override
    protected void updatePayment(Payment payment) {
        stripePaymentRepository.save((StripePayment) payment);
    }

    @Override
    public PaymentResponse cancelPayment(String userId, String orderId) {
        OrderResponse order = enrollmentService.getOrderById(orderId).data();
        if (userId != null) {
            ensureOrderPayableByCurrentUser(order, userId);
        }
        StripePayment payment = stripePaymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment not found for orderId: " + orderId));
        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(ErrorCode.PAYMENT_ALREADY_PAID, "Cannot cancel a paid payment");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentMapper.toDto(stripePaymentRepository.save(payment));
    }

    private Session createCheckoutSession(OrderResponse order, StripePayment payment) {
        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("CINX order " + order.id())
                        .build();
        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(currency)
                        .setUnitAmount(payment.getAmount())
                        .setProductData(productData)
                        .build();
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(resolveUrl(successUrl, order.id()))
                .setCancelUrl(resolveUrl(cancelUrl, order.id()))
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(lineItem)
                .putMetadata("orderId", order.id())
                .build();
        RequestOptions options = RequestOptions.builder()
                .setApiKey(secretKey)
                .setIdempotencyKey("stripe-checkout-session-" + order.id())
                .build();
        try {
            return Session.create(params, options);
        } catch (StripeException e) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Failed to create Stripe checkout session");
        }
    }

    private String resolveUrl(String url, String orderId) {
        return url.replace("{orderId}", orderId);
    }

    private void ensureOrderPayableByCurrentUser(OrderResponse order, String userId) {
        if (order == null) {
            throw new NotFoundException("Order not found");
        }
        if (!Objects.equals(order.userId(), userId)) {
            throw new BadRequestException(ErrorCode.NOT_RESOURCE_OWNER, "You are not allowed to pay this order");
        }
        if (order.paymentMethod() != PaymentMethod.STRIPE) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Order is not configured for STRIPE payment");
        }
    }
}
