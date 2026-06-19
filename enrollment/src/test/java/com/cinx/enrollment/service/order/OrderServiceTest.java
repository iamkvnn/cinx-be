package com.cinx.enrollment.service.order;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ForbiddenException;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentMethod;
import com.cinx.enrollment.dto.request.UpdatePaymentMethodRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.dto.response.OrderResponse;
import com.cinx.enrollment.dto.response.PaymentResponse;
import com.cinx.enrollment.mapper.OrderMapper;
import com.cinx.enrollment.messaging.OrderEventProducer;
import com.cinx.enrollment.model.Order;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.cart.CartService;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.payment.PaymentService;
import com.cinx.enrollment.service.voucher.IVoucherService;
import com.cinx.enrollment.utils.OrderIdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private EnrolledCourseRepository enrolledCourseRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderIdGenerator orderIdGenerator;
    @Mock
    private PaymentService paymentService;
    @Mock
    private CartService cartService;
    @Mock
    private CourseService courseService;
    @Mock
    private OrderEventProducer orderEventProducer;
    @Mock
    private IVoucherService voucherService;
    @InjectMocks
    private OrderService orderService;

    @Test
    void updatePaymentMethodChangesPendingOrderAndUpdatesPaymentSynchronously() {
        Order order = pendingOrder("order-1", "user-1", PaymentMethod.MOMO);
        OrderResponse orderResponse = new OrderResponse(
                order.getId(), order.getUserId(), List.of(), order.getTotalPrice(), order.getDiscounted(),
                order.getOrderDate(), order.getStatus(), PaymentMethod.STRIPE);
        PaymentResponse payment = new PaymentResponse("payment-1", order.getId(), 150_000L, null, null, null, null);
        OrderDetailResponse expected = new OrderDetailResponse(
                order.getId(), order.getUserId(), List.of(), order.getTotalPrice(), order.getDiscounted(),
                order.getOrderDate(), order.getStatus(), PaymentMethod.STRIPE, payment, null);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of());
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);
        when(paymentService.updatePaymentMethod(orderResponse, PaymentMethod.MOMO)).thenReturn(ApiResponse.success("ok", payment));
        when(orderMapper.toDetailDto(any())).thenReturn(expected);

        OrderDetailResponse response = orderService.updatePaymentMethod(
                "user-1", order.getId(), new UpdatePaymentMethodRequest(PaymentMethod.STRIPE));

        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.STRIPE);
        assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.STRIPE);
        verify(paymentService).updatePaymentMethod(orderResponse, PaymentMethod.MOMO);
        verify(paymentService, never()).cancelPayment(any(), any());
        verify(paymentService, never()).createPayment(any());
    }

    @Test
    void updatePaymentMethodRejectsNonOwner() {
        Order order = pendingOrder("order-1", "user-1", PaymentMethod.MOMO);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.updatePaymentMethod(
                "user-2", order.getId(), new UpdatePaymentMethodRequest(PaymentMethod.STRIPE)))
                .isInstanceOf(ForbiddenException.class);

        verify(paymentService, never()).cancelPayment(any(), any());
        verify(orderEventProducer, never()).publishOrderCreatedEvent(any());
    }

    @Test
    void updatePaymentMethodRejectsNonPendingOrder() {
        Order order = pendingOrder("order-1", "user-1", PaymentMethod.MOMO);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.updatePaymentMethod(
                "user-1", order.getId(), new UpdatePaymentMethodRequest(PaymentMethod.STRIPE)))
                .isInstanceOf(BadRequestException.class);

        verify(paymentService, never()).cancelPayment(any(), any());
        verify(orderEventProducer, never()).publishOrderCreatedEvent(any());
    }

    @Test
    void updatePaymentMethodRejectsSamePaymentMethod() {
        Order order = pendingOrder("order-1", "user-1", PaymentMethod.MOMO);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(order.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.updatePaymentMethod(
                "user-1", order.getId(), new UpdatePaymentMethodRequest(PaymentMethod.MOMO)))
                .isInstanceOf(BadRequestException.class);

        verify(paymentService, never()).cancelPayment(any(), any());
        verify(orderEventProducer, never()).publishOrderCreatedEvent(any());
    }

    private Order pendingOrder(String orderId, String userId, PaymentMethod paymentMethod) {
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(200_000L);
        order.setDiscounted(50_000L);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        return order;
    }
}
