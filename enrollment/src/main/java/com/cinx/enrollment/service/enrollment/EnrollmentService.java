package com.cinx.enrollment.service.enrollment;

import com.cinx.common.utils.AuthenticationUtil;
import com.cinx.enrollment.dto.response.CheckEnrollmentStatus;
import com.cinx.enrollment.dto.response.CourseResponse;
import com.cinx.enrollment.model.OrderItem;
import com.cinx.enrollment.repository.OrderItemRepository;
import com.cinx.enrollment.service.course.CourseService;
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

    @Override
    public Page<CourseResponse> getEnrolledCourses(int page, int size) {
        String userId = AuthenticationUtil.extractUserId();
        Page<OrderItem> orderItems = orderItemRepository.findAllByUserId(userId, PageRequest.of(page - 1, size));
        List<String> courseIds = orderItems.stream()
            .map(OrderItem::getCourseId)
            .toList();
        List<CourseResponse> courses = courseService.getCoursesByIds(courseIds).data();
        return new PageImpl<>(courses, PageRequest.of(page, size), orderItems.getTotalElements());
    }

    @Override
    public List<CheckEnrollmentStatus> checkEnrollmentStatus(List<String> courseIds) {
        String userId = AuthenticationUtil.extractUserId();
        List<OrderItem> orderItems = orderItemRepository.findAllByCourseIdsAndUserId(courseIds, userId);
        Map<String, OrderItem> orderItemMap = orderItems.stream()
            .collect(Collectors.toMap(OrderItem::getCourseId, Function.identity(), (existing, replacement) -> existing));
        return courseIds.stream()
            .map(courseId -> new CheckEnrollmentStatus(courseId, orderItemMap.containsKey(courseId)))
            .toList();
    }
}
