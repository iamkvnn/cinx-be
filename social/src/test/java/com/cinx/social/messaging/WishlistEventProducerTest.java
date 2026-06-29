package com.cinx.social.messaging;

import com.cinx.social.messaging.event.WishlistEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WishlistEventProducerTest {
    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Test
    void enqueuesWishlistAddedToSocialExchange() {
        WishlistEventProducer producer = new WishlistEventProducer(outboxEventPublisher);
        WishlistEvent event = new WishlistEvent("user-1", "course-1", true);

        producer.publishWishlistAddedEvent(event);

        verify(outboxEventPublisher).enqueue(
                anyString(),
                eq("Wishlist"),
                eq("course-1"),
                eq("WishlistAdded"),
                eq("social.events.exchange"),
                eq("social.wishlist.added"),
                same(event)
        );
    }

    @Test
    void enqueuesWishlistRemovedToSocialExchange() {
        WishlistEventProducer producer = new WishlistEventProducer(outboxEventPublisher);
        WishlistEvent event = new WishlistEvent("user-1", "course-1", false);

        producer.publishWishlistRemovedEvent(event);

        verify(outboxEventPublisher).enqueue(
                anyString(),
                eq("Wishlist"),
                eq("course-1"),
                eq("WishlistRemoved"),
                eq("social.events.exchange"),
                eq("social.wishlist.removed"),
                same(event)
        );
    }
}
