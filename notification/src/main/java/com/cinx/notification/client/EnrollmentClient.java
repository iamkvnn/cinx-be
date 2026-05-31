package com.cinx.notification.client;

import com.cinx.common.dto.ApiResponse;
import com.cinx.notification.dto.response.enrollment.OrderDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "enrollment", path = "/internal")
public interface EnrollmentClient {
    @GetMapping("/orders/{orderId}")
    ApiResponse<OrderDetailResponse> getOrderById(@PathVariable("orderId") String orderId);
    
    @GetMapping("/enrollments/courses/{courseId}/users")
    ApiResponse<java.util.List<String>> getUserIdsEnrolledInCourse(@PathVariable("courseId") String courseId);
}
