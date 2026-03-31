package com.cinx.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("notification.order.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "notification.order.dead")
                .withArgument("x-message-ttl", 60000)          // 60s TTL
                .withArgument("x-max-length", 10000)           // backpressure
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("notification.order.dead.queue").build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange("order.events.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("dlx.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with("order.order.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.order.dead");
    }

    // --- Learning Events Configuration ---

    @Bean
    public Queue learningReminderQueue() {
        return QueueBuilder.durable("notification.learning.reminder.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "notification.learning.dead")
                .build();
    }

    @Bean
    public TopicExchange learningEventsExchange() {
        return ExchangeBuilder.topicExchange("learning.events.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public Binding learningReminderBinding() {
        return BindingBuilder.bind(learningReminderQueue())
                .to(learningEventsExchange())
                .with("learning.reminder.#");
    }
}
