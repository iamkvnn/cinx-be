package com.cinx.payment.service.enrollment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.payment.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "enrollment", path = "/api/v1")
public interface EnrollmentService {
    @GetMapping("/orders/{orderId}")
    ApiResponse<OrderResponse> getOrderById(@RequestHeader("X-User-Id") String userId, @PathVariable String orderId);
}
