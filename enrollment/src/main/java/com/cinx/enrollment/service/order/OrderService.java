package com.cinx.enrollment.service.order;

import com.cinx.common.exception.BadRequestException;
import com.cinx.common.exception.ErrorCode;
import com.cinx.common.exception.NotFoundException;
import com.cinx.common.mapper.SortConverter;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.consts.PaymentMethod;
import com.cinx.enrollment.dto.request.CartItemDto;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.request.UpdatePaymentMethodRequest;
import com.cinx.enrollment.dto.response.*;
import com.cinx.enrollment.mapper.OrderMapper;
import com.cinx.enrollment.messaging.OrderEventProducer;
import com.cinx.enrollment.model.Order;
import com.cinx.enrollment.model.OrderItem;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.repository.OrderRepository;
import com.cinx.enrollment.repository.EnrolledCourseRepository;
import com.cinx.enrollment.service.cart.CartService;
import com.cinx.enrollment.service.course.CourseService;
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
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EnrolledCourseRepository enrolledCourseRepository;
    private final OrderMapper orderMapper;
    private final OrderIdGenerator orderIdGenerator;
    private final PaymentService paymentService;
    private final CartService cartService;
    private final CourseService courseService;
    private final OrderEventProducer orderEventProducer;
    private final IVoucherService voucherService;

    @Override
    public Page<OrderDetailResponse> getOrdersByUserId(String userId, int page, int size, String query, String sort) {
        Sort s = SortConverter.toSort(sort);
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
    public OrderDetailResponse getOrderById(String userId, String orderId) {
        Order order = getOrderEntity(orderId);
        ensureCurrentUserOwns(userId, order);
        return toDetailResponse(order);
    }

    @Override
    public OrderDetailResponse getInternalOrderById(String orderId) {
        return toDetailResponse(getOrderEntity(orderId));
    }

    private Order getOrderEntity(String orderId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setItems(orderItemRepository.findAllByOrderId(order.getId()));
                    return order;
                })
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private OrderDetailResponse toDetailResponse(Order order) {
        try {
            PaymentResponse payment = paymentService.getPaymentByOrderId(order.getId(), order.getPaymentMethod()).data();
            return orderMapper.toDetailDto(new OrderAggregate(order, payment));
        }
        catch (Exception e) {
            System.out.println("No payment found for order " + order.getId() + ": " + e.getMessage());
            return orderMapper.toDetailDto(new OrderAggregate(order, null));
        }
    }

    @Transactional
    @Override
    public OrderDetailResponse createOrder(String userId, CreateOrderRequest request) {
        validateCreateOrderRequest(request);
        List<OrderItem> orderItems = createOrderItems(request);
        List<String> alreadyEnrolledCourseIds = orderItems.stream()
                .map(OrderItem::getCourseId)
                .filter(courseId -> enrolledCourseRepository.existsByCourseIdAndUserId(courseId, userId))
                .toList();
        if (!alreadyEnrolledCourseIds.isEmpty()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Cart contains course(s) already enrolled: " + String.join(", ", alreadyEnrolledCourseIds));
        }
        Long totalPrice = orderItems.stream().mapToLong(OrderItem::getPrice).sum();
        long discounted = orderItems.stream().mapToLong(item -> item.getPrice() - item.getDiscountedPrice()).sum();
        VoucherResponse voucherResponse = null;
        if (request.voucherCode() != null) {
            voucherResponse = voucherService.validateVoucher(request.voucherCode(), totalPrice);
            long discountAmount = voucherResponse.discountAmount() * (totalPrice - discounted) / 100;
            discounted += Math.min(discountAmount, voucherResponse.maxDiscountAmount());
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
        cartService.removeAllFromCartByIds(userId, request.cartItems().stream().map(CartItemDto::id).toList());
        PaymentResponse payment = paymentService.createPayment(orderMapper.toDto(order)).data();
        orderEventProducer.publishOrderCreatedEvent(orderMapper.toEvent(order));
        return orderMapper.toDetailDto(new OrderAggregate(order, payment));
    }

    @Transactional
    @Override
    public OrderDetailResponse updatePaymentMethod(String userId, String orderId, UpdatePaymentMethodRequest request) {
        Order order = getOrderEntity(orderId);
        ensureCurrentUserOwns(userId, order);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Only PENDING orders can change payment method");
        }
        PaymentMethod oldPaymentMethod = order.getPaymentMethod();
        if (oldPaymentMethod == request.paymentMethod()) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Payment method is already " + request.paymentMethod());
        }

        order.setPaymentMethod(request.paymentMethod());
        Order savedOrder = orderRepository.save(order);
        PaymentResponse payment = paymentService.updatePaymentMethod(orderMapper.toDto(savedOrder), oldPaymentMethod).data();
        return orderMapper.toDetailDto(new OrderAggregate(savedOrder, payment));
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

    @Transactional
    @Override
    public OrderDetailResponse cancelOrder(String userId, String orderId) {
        Order order = getOrderEntity(orderId);
        ensureCurrentUserOwns(userId, order);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(ErrorCode.BAD_REQUEST, "Only PENDING orders can be cancelled");
        }
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.cartItems() == null || request.cartItems().isEmpty()) {
            throw new BadRequestException(ErrorCode.ORDER_ITEMS_REQUIRED, "Order must contain at least one item");
        }
    }

    private List<OrderItem> createOrderItems(CreateOrderRequest request) {
        List<String> courseIds = request.cartItems().stream()
                .map(item -> item.course().id())
                .distinct()
                .toList();
        Map<String, CourseResponse> coursesById = courseService.getCoursesByIds(courseIds).data().stream()
                .collect(Collectors.toMap(CourseResponse::id, course -> course));
        if (coursesById.size() != courseIds.size()) {
            throw new BadRequestException(ErrorCode.COURSE_UNAVAILABLE_FOR_PURCHASE, "Some courses are no longer available for purchase");
        }
        return courseIds.stream()
                .map(courseId -> {
                    CourseResponse courseResponse = coursesById.get(courseId);
                    return OrderItem.builder()
                            .courseId(courseResponse.id())
                            .instructorId(courseResponse.instructor() != null ? courseResponse.instructor().id() : null)
                            .title(courseResponse.title())
                            .price(courseResponse.price())
                            .discountedPrice(courseResponse.discountedPrice())
                            .build();
                })
                .toList();
    }

    private void ensureCurrentUserOwns(String userId, Order order) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new com.cinx.common.exception.ForbiddenException(ErrorCode.NOT_RESOURCE_OWNER, "You are not allowed to access this order");
        }
    }
}
