package com.cinx.cart.messaging;

import com.cinx.cart.messaging.event.CourseArchivedEvent;
import com.cinx.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseArchivedListener {
    private final CartItemRepository cartItemRepository;

    @Transactional
    @RabbitListener(queues = "cart.course-archived.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleCourseArchived(CourseArchivedEvent event) {
        if (event == null || event.course() == null || event.course().id() == null) {
            return;
        }
        cartItemRepository.deleteAllByCourseId(event.course().id());
        log.info("Removed archived course from carts - courseId={}", event.course().id());
    }
}
