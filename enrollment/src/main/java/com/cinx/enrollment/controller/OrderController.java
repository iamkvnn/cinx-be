package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.dto.PaginatedApiResponse;
import com.cinx.common.mapper.PaginationWrapper;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.dto.request.CreateOrderRequest;
import com.cinx.enrollment.dto.request.UpdatePaymentMethodRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.service.order.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Operation(summary = "Get orders by user id for admin", security = @SecurityRequirement(name = "bearer-jwt"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}")
    public ResponseEntity<PaginatedApiResponse<OrderDetailResponse>> getOrdersByUserIdForAdmin(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sort
    ) {
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
    public ResponseEntity<ApiResponse<OrderDetailResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String userId = AuthenticationUtil.extractUserId();
        OrderDetailResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", response));
    }

    @Operation(summary = "", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping("/{orderId}/payment-method")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updatePaymentMethod(
            @PathVariable String orderId,
            @Valid @RequestBody UpdatePaymentMethodRequest request
    ) {
        String userId = AuthenticationUtil.extractUserId();
        OrderDetailResponse response = orderService.updatePaymentMethod(userId, orderId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Payment method updated successfully", response));
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
