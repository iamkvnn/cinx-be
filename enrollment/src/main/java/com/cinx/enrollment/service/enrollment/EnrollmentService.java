package com.cinx.enrollment.service.enrollment;

import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.consts.PaymentStatus;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.dto.response.PaymentResponse;
import com.cinx.enrollment.model.OrderItem;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements IEnrollmentService {
    private final OrderItemRepository orderItemRepository;
    private final CourseService courseService;
    private final PaymentService paymentService;

    @Override
    public Page<CourseResponse> getEnrolledCourses(int page, int size) {
        String userId = AuthenticationUtil.extractUserId();
        Page<OrderItem> orderItems = orderItemRepository.findAllByUserId(userId, PageRequest.of(page - 1, size));
        List<String> orderIds = orderItems.stream()
            .map(OrderItem::getOrderId)
            .toList();
        Map<String, PaymentResponse> paymentMap = paymentService.getPaymentByIds(orderIds).data().stream()
            .filter(payment -> payment.status().equals(PaymentStatus.PAID))
            .collect(Collectors.toMap(PaymentResponse::orderId, Function.identity()));
        List<OrderItem> paidCourse = orderItems.stream()
            .filter(orderItem -> paymentMap.containsKey(orderItem.getOrderId()))
            .toList();
        List<String> courseIds = paidCourse.stream()
            .map(OrderItem::getCourseId)
            .toList();
        List<CourseResponse> courses = courseService.getCoursesByIds(courseIds).data();
        return new PageImpl<>(courses, PageRequest.of(page, size), paidCourse.size());
    }

    @Override
    public List<CheckEnrollmentStatus> checkEnrollmentStatus(List<String> courseIds) {
        String userId = AuthenticationUtil.extractUserId();
        List<OrderItem> orderItems = orderItemRepository.findAllByCourseIdsAndUserId(courseIds, userId);
        Map<String, OrderItem> orderItemMap = orderItems.stream()
            .collect(Collectors.toMap(OrderItem::getCourseId, Function.identity(), (existing, replacement) -> existing));
        List<String> paidCourseIds = orderItems.stream()
            .map(OrderItem::getOrderId)
            .toList();
        Map<String, PaymentResponse> paymentMap = paymentService.getPaymentByIds(paidCourseIds).data().stream()
            .filter(payment -> payment.status().equals(PaymentStatus.PAID))
            .collect(Collectors.toMap(PaymentResponse::orderId, Function.identity()));
        return courseIds.stream()
            .map(courseId -> new CheckEnrollmentStatus(courseId, orderItemMap.containsKey(courseId) && paymentMap.containsKey(orderItemMap.get(courseId).getOrderId())))
            .toList();
    }
}
