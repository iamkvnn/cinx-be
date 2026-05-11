package com.cinx.enrollment.messaging;


import com.cinx.enrollment.consts.OrderStatus;
import com.cinx.enrollment.dto.request.CreateEnrolledCourseRequest;
import com.cinx.enrollment.dto.response.OrderDetailResponse;
import com.cinx.enrollment.messaging.event.PaymentEvent;
import com.cinx.enrollment.service.course.CourseService;
import com.cinx.enrollment.service.enrollment.IEnrollmentService;
import com.cinx.enrollment.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {
    private final IEnrollmentService enrollmentService;
    private final IOrderService orderService;
    private final CourseService courseService;

    @RabbitListener(queues = "enrollment.payment.queue", containerFactory = "rabbitListenerContainerFactory")
    public void receivePaymentMessage(PaymentEvent paymentEvent) {
        System.out.println("Received payment message: " + paymentEvent);
        OrderDetailResponse orderDetail = orderService.updateOrderStatus(paymentEvent.getOrderId(), OrderStatus.PAID);
        enrollmentService.enrollCourses(orderDetail.items().stream()
                .map(item ->
                        new CreateEnrolledCourseRequest(item.courseId(), orderDetail.userId())
                ).toList()
        );
        orderDetail.items().forEach(item -> {
                System.out.println("Increasing enrollment count for course: " + item.courseId());
                courseService.increaseEnrollmentCount(item.courseId());
        });
    }
}
