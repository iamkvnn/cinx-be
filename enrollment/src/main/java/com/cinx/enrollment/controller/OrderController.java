package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.dto.response.OrderResponse;
import com.cinx.enrollment.service.order.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrderService orderService;

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping
    public ResponseEntity<PaginatedApiResponse<OrderDetailResponse>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
        String userId = AuthenticationUtil.extractUserId();
        Page<OrderDetailResponse> orders = orderService.getOrdersByUserId(userId, page, size, query, sort);
        return ResponseEntity.ok(PaginationWrapper.wrap(orders));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderById(@PathVariable String orderId) {
        String userId = AuthenticationUtil.extractUserId();
        return ResponseEntity.ok(new ApiResponse<>(true, "", orderService.getOrderById(userId, orderId)));
    }


    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", response));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @PathVariable String orderId
    ) {
        String userId = AuthenticationUtil.extractUserId();
        OrderDetailResponse response = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order cancelled successfully", response));
    }
}
