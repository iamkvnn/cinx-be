package com.cinx.social.messaging;

import com.cinx.social.messaging.event.CourseArchivedEvent;
import com.cinx.social.messaging.event.WishlistEvent;
import com.cinx.social.model.WishlistItem;
import com.cinx.social.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseArchivedListener {
    private final WishlistItemRepository wishlistItemRepository;
    private final WishlistEventProducer wishlistEventProducer;

    @Transactional
    @RabbitListener(queues = "social.course-archived.queue", containerFactory = "rabbitListenerContainerFactory")
    public void handleCourseArchived(CourseArchivedEvent event) {
        if (event == null || event.course() == null || event.course().id() == null) {
            return;
        }
        String courseId = event.course().id();
        List<WishlistItem> wishlistItems = wishlistItemRepository.findByCourseId(courseId);
        wishlistItemRepository.deleteAll(wishlistItems);
        wishlistItems.forEach(item ->
                wishlistEventProducer.publishWishlistRemovedEvent(new WishlistEvent(item.getUserId(), courseId, false))
        );
        log.info("Removed archived course from wishlists - courseId={}, itemCount={}", courseId, wishlistItems.size());
    }
}
