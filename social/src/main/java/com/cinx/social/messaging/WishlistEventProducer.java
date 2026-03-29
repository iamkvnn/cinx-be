package com.cinx.social.messaging;

import com.cinx.social.messaging.event.WishlistEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WishlistEventProducer {
    private final RabbitTemplate rabbitTemplate;

    public void publishWishlistAddedEvent(WishlistEvent event) {
        System.out.println("Publishing wishlist added event: " + event);
        rabbitTemplate.convertAndSend("social.events.exchange", "social.wishlist.added", event);
    }

    public void publishWishlistRemovedEvent(WishlistEvent event) {
        System.out.println("Publishing wishlist removed event: " + event);
        rabbitTemplate.convertAndSend("social.events.exchange", "social.wishlist.removed", event);
    }
}
