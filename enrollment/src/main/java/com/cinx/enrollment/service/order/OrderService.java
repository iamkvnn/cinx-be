package com.cinx.enrollment.service.order;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.request.CartItemDto;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.response.*;
import com.cinx.enrollment.mapper.OrderMapper;
import com.cinx.enrollment.messaging.OrderEventProducer;
import com.cinx.enrollment.model.Order;
import com.cinx.enrollment.model.OrderItem;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.cart.CartService;
import com.cinx.enrollment.service.payment.PaymentService;
import com.cinx.enrollment.service.voucher.IVoucherService;
import com.cinx.enrollment.utils.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderIdGenerator orderIdGenerator;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final OrderEventProducer orderEventProducer;
    private final IVoucherService voucherService;

    @Override
    public Page<OrderDetailResponse> getOrdersByUserId(int page, int size, String query, String sort) {
        Sort s = SortConverter.toSort(sort);
        String userId = AuthenticationUtil.extractUserId();
        Page<Order> orders = orderRepository.findAllByUserIdAndQuery(userId, query, PageRequest.of(page - 1, size, s));
        List<String> orderIds = orders.stream().map(Order::getId).toList();
        Map<String, PaymentResponse> payments = paymentService.getPaymentByIds(orderIds).data().stream()
                .collect(Collectors.toMap(PaymentResponse::orderId, p -> p));
        return orders.map(order -> {
            order.setItems(orderItemRepository.findAllByOrderId(order.getId()));
            PaymentResponse payment = payments.get(order.getId());
            return orderMapper.toDetailDto(new OrderAggregate(order, payment));
        });
    }

    @Override
    public OrderDetailResponse getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setItems(orderItemRepository.findAllByOrderId(order.getId()));
                    return order;
                })
                .map(o ->{
                    try {
                        PaymentResponse payment = paymentService.getPaymentByOrderId(o.getId(), o.getPaymentMethod()).data();
                        return orderMapper.toDetailDto(new OrderAggregate(o, payment));
                    }
                    catch (Exception e) {
                        System.out.println("No payment found for order " + o.getId() + ": " + e.getMessage());
                        return orderMapper.toDetailDto(new OrderAggregate(o, null));
                    }
                })
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        validateCreateOrderRequest(request);
        List<OrderItem> orderItems = createOrderItems(request);
        Long totalPrice = orderItems.stream().mapToLong(OrderItem::getPrice).sum();
        Long discounted = orderItems.stream().mapToLong(item -> item.getPrice() - item.getDiscountedPrice()).sum();
        VoucherResponse voucherResponse = null;
        if (request.voucherCode() != null) {
            voucherResponse = voucherService.validateVoucher(request.voucherCode(), totalPrice);
            discounted += voucherResponse.discountAmount();
        }
        Order order = orderRepository.save(
                Order.builder()
                        .userId(userId)
                        .orderDate(LocalDateTime.now())
                        .totalPrice(totalPrice)
                        .discounted(discounted)
                        .paymentMethod(request.paymentMethod())
                        .status(OrderStatus.PENDING)
                        .voucherId(voucherResponse != null ? voucherResponse.id() : null)
                        .build()
        );
        orderItems.forEach(item -> item.setOrderId(order.getId()));
        order.setItems(orderItems);
        orderItemRepository.saveAll(orderItems);
        cartService.removeAllFromCartByIds(request.cartItems().stream().map(CartItemDto::id).toList());
        orderEventProducer.publishOrderCreatedEvent(orderMapper.toEvent(order));
        return orderMapper.toDto(order);
    }

    @Transactional
    @Override
    public OrderDetailResponse updateOrderStatus(String orderId, OrderStatus status) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(status);
                    orderRepository.save(order);
                    if (status == OrderStatus.CANCELLED) {
                        paymentService.cancelPayment(order.getId(), order.getPaymentMethod());
                        orderEventProducer.publishOrderCancelledEvent(orderMapper.toEvent(order));
                    }
                    return order;
                })
                .map(o -> {
                    o.setItems(orderItemRepository.findAllByOrderId(o.getId()));
                    try {
                        PaymentResponse payment = paymentService.getPaymentByOrderId(o.getId(), o.getPaymentMethod()).data();
                        return orderMapper.toDetailDto(new OrderAggregate(o, payment));
                    }
                    catch (Exception e) {
                        return orderMapper.toDetailDto(new OrderAggregate(o, null));
                    }
                })
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.cartItems() == null || request.cartItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
    }

    private List<OrderItem> createOrderItems(CreateOrderRequest request) {
        return request.cartItems().stream()
                .map(item -> OrderItem.builder()
                        .courseId(item.course().id())
                        .title(item.course().title())
                        .price(item.course().price())
                        .discountedPrice(item.course().discountedPrice())
                        .build())
                .toList();
    }
}
