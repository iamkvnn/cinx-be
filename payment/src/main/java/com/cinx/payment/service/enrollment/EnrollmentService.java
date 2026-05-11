package com.cinx.payment.service.enrollment;

import com.cinx.common.dto.ApiResponse;
import com.cinx.payment.config.FeignConfig;
import com.cinx.payment.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "enrollment", path = "/internal", configuration = FeignConfig.class)
public interface EnrollmentService {
    @GetMapping("/orders/{orderId}")
    ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId);
}
