package com.cinx.enrollment.service.order;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.NotFoundException;
import com.cinx.enrollment.dto.request.CartItemDto;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.response.*;
import com.cinx.enrollment.mapper.OrderMapper;
import com.cinx.enrollment.model.Order;
import com.cinx.enrollment.model.OrderItem;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.service.cart.CartService;
import com.cinx.enrollment.service.payment.PaymentService;
import com.cinx.enrollment.utils.OrderIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderIdGenerator orderIdGenerator;
    private final PaymentService paymentService;
    private final CartService cartService;

    @Override
    public Page<OrderResponse> getOrdersByUserId(String userId, int page, int size) {
        return orderRepository.findAllByUserId(userId, PageRequest.of(page - 1, size))
                .map(order -> {
                    order.setItems(orderItemRepository.findAllByOrderId(order.getId()));
                    return order;
                })
                .map(orderMapper::toDto);
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
                        System.out.println("Payment found for order " + o.getId() + ": " + payment);
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
    public void createOrder(String userId, CreateOrderRequest request) {
        validateCreateOrderRequest(request);
        List<OrderItem> orderItems = createOrderItems(request);
        Long totalPrice = orderItems.stream().mapToLong(OrderItem::getPrice).sum();
        Long discounted = orderItems.stream().mapToLong(item -> item.getPrice() - item.getDiscountedPrice()).sum();
        Order order = orderRepository.save(
                Order.builder()
                        .userId(userId)
                        .orderDate(LocalDateTime.now())
                        .totalPrice(totalPrice)
                        .discounted(discounted)
                        .paymentMethod(request.paymentMethod())
                        .build()
        );
        orderItems.forEach(item -> item.setOrderId(order.getId()));
        orderItemRepository.saveAll(orderItems);
        cartService.removeAllFromCartByIds(userId, request.cartItems().stream().map(CartItemDto::id).toList());
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
