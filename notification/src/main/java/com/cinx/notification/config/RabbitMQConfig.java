package com.cinx.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Exchanges ---
    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange("order.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange authEventsExchange() {
        return ExchangeBuilder.topicExchange("auth.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange learningEventsExchange() {
        return ExchangeBuilder.topicExchange("learning.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return ExchangeBuilder.topicExchange("user.events.exchange").durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("dlx.exchange").durable(true).build();
    }

    // --- Dead Letter Queue ---
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("notification.dlq").build();
    }

    // --- Channel Queues ---
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("notification.email.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "email.dead")
                .build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable("notification.push.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "push.dead")
                .build();
    }

    @Bean
    public Queue inAppQueue() {
        return QueueBuilder.durable("notification.inapp.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "inapp.dead")
                .build();
    }

    // --- Bindings ---

    @Bean
    public Binding emailAuthBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(authEventsExchange())
                .with("auth.email.#");
    }

    @Bean
    public Binding pushOrderBinding() {
        return BindingBuilder.bind(pushQueue())
                .to(orderExchange())
                .with("order.order.#");
    }

    @Bean
    public Binding inAppOrderBinding() {
        return BindingBuilder.bind(inAppQueue())
                .to(orderExchange())
                .with("order.order.#");
    }

    @Bean
    public Binding pushLearningBinding() {
        return BindingBuilder.bind(pushQueue())
                .to(learningEventsExchange())
                .with("learning.reminder.#");
    }

    @Bean
    public Binding inAppLearningBinding() {
        return BindingBuilder.bind(inAppQueue())
                .to(learningEventsExchange())
                .with("learning.reminder.#");
    }

    @Bean
    public Binding emailUserBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(userEventsExchange())
                .with("user.email.#");
    }

    @Bean
    public Binding dlqEmailBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("email.dead");
    }
    
    @Bean
    public Binding dlqPushBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("push.dead");
    }

    @Bean
    public Binding dlqInAppBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("inapp.dead");
    }
}
