package com.cinx.enrollment.controller;

import com.cinx.common.dto.ApiResponse;
import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import com.cinx.enrollment.service.order.IOrderService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API — called only by other services via Feign (service-to-service).
 * Not exposed externally; blocked at the gateway layer (/internal/** → denyAll).
 */
@Hidden
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalEnrollmentController {

    private final IEnrollmentService enrollmentService;
    private final IOrderService orderService;

    @PostMapping("/enrollments/check")
    public ApiResponse<List<CheckEnrollmentStatus>> checkEnrollmentStatus(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody List<String> courseIds
    ) {
        return new ApiResponse<>(true, "Enrollment status fetched successfully",
                enrollmentService.checkEnrollmentStatus(userId, courseIds));
    }

    @PostMapping("/enrollments")
    public ApiResponse<Void> enrollCourses(@RequestBody List<CreateEnrolledCourseRequest> requests) {
        enrollmentService.enrollCourses(requests);
        return new ApiResponse<>(true, "Enrolled successfully", null);
    }

    @GetMapping("/enrollments/courses/{courseId}/users")
    public ApiResponse<List<String>> getUserIdsEnrolledInCourse(@PathVariable String courseId) {
        return new ApiResponse<>(true, "Users fetched successfully", enrollmentService.getUserIdsEnrolledInCourse(courseId));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderById(@PathVariable String orderId) {
        return new ApiResponse<>(true, "Order fetched successfully", orderService.getInternalOrderById(orderId));
    }
}
