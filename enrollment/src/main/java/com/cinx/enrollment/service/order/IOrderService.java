package com.cinx.enrollment.service.order;

import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.dto.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface IOrderService {
    Page<OrderDetailResponse> getOrdersByUserId(int page, int size);
    OrderDetailResponse getOrderById(String orderId);
    OrderResponse createOrder(CreateOrderRequest request);
}
