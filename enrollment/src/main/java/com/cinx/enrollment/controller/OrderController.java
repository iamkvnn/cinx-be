package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.response.OrderResponse;
import com.cinx.enrollment.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @GetMapping
    public ResponseEntity<PaginatedApiResponse<?>> getOrders(@RequestHeader("X-User-Id") String userId) {
        Page<OrderResponse> orders = orderService.getOrdersByUserId(userId, 1, 10);
        return ResponseEntity.ok(PaginationWrapper.wrap(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getOrderById(@RequestHeader("X-User-Id") String userId, @PathVariable String orderId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "", orderService.getOrderById(orderId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(@RequestHeader("X-User-Id") String userId, @RequestBody CreateOrderRequest request) {
        orderService.createOrder(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", null));
    }
}
