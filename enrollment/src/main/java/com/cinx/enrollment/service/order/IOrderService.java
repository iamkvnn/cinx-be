package com.cinx.enrollment.service.order;

import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.request.UpdatePaymentMethodRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import org.springframework.data.domain.Page;

public interface IOrderService {
    Page<OrderDetailResponse> getOrdersByUserId(String userId, int page, int size, String query, String sort);
    OrderDetailResponse getOrderById(String userId, String orderId);
    OrderDetailResponse getInternalOrderById(String orderId);
    OrderDetailResponse createOrder(String userId, CreateOrderRequest request);
    OrderDetailResponse updatePaymentMethod(String userId, String orderId, UpdatePaymentMethodRequest request);
    OrderDetailResponse updateOrderStatus(String orderId, OrderStatus status);
    OrderDetailResponse cancelOrder(String userId, String orderId);
}
