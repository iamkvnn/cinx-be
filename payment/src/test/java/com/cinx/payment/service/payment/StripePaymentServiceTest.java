package com.cinx.payment.service.payment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.payment.consts.OrderStatus;
import com.cinx.payment.consts.PaymentMethod;
import com.cinx.payment.consts.PaymentStatus;
import com.cinx.payment.dto.response.OrderResponse;
import com.cinx.payment.mapper.PaymentMapper;
import com.cinx.payment.messaging.PaymentEventProducer;
import com.cinx.payment.model.StripePayment;
import com.cinx.payment.repository.StripePaymentRepository;
import com.cinx.payment.service.enrollment.EnrollmentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentServiceTest {
    @Mock
    private StripePaymentRepository stripePaymentRepository;
    @Mock
    private EnrollmentService enrollmentService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentEventProducer paymentEventProducer;
    @InjectMocks
    private StripePaymentService stripePaymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stripePaymentService, "secretKey", "sk_test_123");
        ReflectionTestUtils.setField(stripePaymentService, "webhookSecret", "whsec_123");
        ReflectionTestUtils.setField(stripePaymentService, "successUrl", "https://app.test/thanks/{orderId}");
        ReflectionTestUtils.setField(stripePaymentService, "cancelUrl", "https://app.test/checkout/{orderId}");
        ReflectionTestUtils.setField(stripePaymentService, "currency", "vnd");
    }

    @Test
    void createPaymentUsesOrderNetAmount() {
        OrderResponse order = order();
        StripePayment saved = StripePayment.builder()
                .orderId(order.id())
                .amount(150_000L)
                .status(PaymentStatus.PROCESSING)
                .build();
        when(stripePaymentRepository.save(any(StripePayment.class))).thenReturn(saved);

        StripePayment payment = stripePaymentService.createPayment(order);

        assertThat(payment.getAmount()).isEqualTo(150_000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
    }

    @Test
    void getCheckoutLinkReturnsCachedUrlWhenStillValid() {
        OrderResponse order = order();
        StripePayment payment = StripePayment.builder()
                .orderId(order.id())
                .amount(150_000L)
                .paymentUrl("https://checkout.stripe.test/session")
                .urlExpireTime(LocalDateTime.now().plusMinutes(10))
                .status(PaymentStatus.PROCESSING)
                .build();
        payment.setId("payment-1");
        when(enrollmentService.getOrderById(order.id())).thenReturn(ApiResponse.success("ok", order));
        when(stripePaymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        String url = stripePaymentService.getCheckoutLink(order.userId(), payment.getId());

        assertThat(url).isEqualTo("https://checkout.stripe.test/session");
    }

    @Test
    void getCheckoutLinkCreatesCheckoutSessionWhenNoCachedUrl() {
        OrderResponse order = order();
        StripePayment payment = StripePayment.builder()
                .orderId(order.id())
                .amount(150_000L)
                .status(PaymentStatus.PROCESSING)
                .build();
        payment.setId("payment-1");
        Session session = new Session();
        session.setId("cs_test_123");
        session.setUrl("https://checkout.stripe.test/session");
        session.setExpiresAt(1_900_000_000L);

        when(enrollmentService.getOrderById(order.id())).thenReturn(ApiResponse.success("ok", order));
        when(stripePaymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(stripePaymentRepository.save(any(StripePayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
            sessionMock.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(session);

            String url = stripePaymentService.getCheckoutLink(order.userId(), payment.getId());

            assertThat(url).isEqualTo("https://checkout.stripe.test/session");
            assertThat(payment.getCheckoutSessionId()).isEqualTo("cs_test_123");
            assertThat(payment.getPaymentUrl()).isEqualTo("https://checkout.stripe.test/session");
        }
    }

    @Test
    void getCheckoutLinkRejectsOrderConfiguredForOtherMethod() {
        OrderResponse order = new OrderResponse(
                "order-1", "user-1", List.of(), 200_000L, 50_000L,
                LocalDateTime.now(), OrderStatus.PENDING, PaymentMethod.MOMO);
        StripePayment payment = StripePayment.builder()
                .orderId(order.id())
                .amount(150_000L)
                .status(PaymentStatus.PROCESSING)
                .build();
        payment.setId("payment-1");
        when(stripePaymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(enrollmentService.getOrderById(order.id())).thenReturn(ApiResponse.success("ok", order));

        assertThatThrownBy(() -> stripePaymentService.getCheckoutLink(order.userId(), payment.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    private OrderResponse order() {
        return new OrderResponse(
                "order-1", "user-1", List.of(), 200_000L, 50_000L,
                LocalDateTime.now(), OrderStatus.PENDING, PaymentMethod.STRIPE);
    }
}
