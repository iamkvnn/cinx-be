package com.cinx.social.messaging;

import com.cinx.social.messaging.event.WishlistEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WishlistEventProducer {
    private static final String EXCHANGE = "social.events.exchange";

    private final OutboxEventPublisher outboxEventPublisher;

    public void publishWishlistAddedEvent(WishlistEvent event) {
        System.out.println("Publishing wishlist added event: " + event);
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "Wishlist",
                event.getCourseId(),
                "WishlistAdded",
                EXCHANGE,
                "social.wishlist.added",
                event
        );
    }

    public void publishWishlistRemovedEvent(WishlistEvent event) {
        System.out.println("Publishing wishlist removed event: " + event);
        outboxEventPublisher.enqueue(
                UUID.randomUUID().toString(),
                "Wishlist",
                event.getCourseId(),
                "WishlistRemoved",
                EXCHANGE,
                "social.wishlist.removed",
                event
        );
    }
}
