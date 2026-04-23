package com.cinx.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Exchanges ---
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange("notification.send.exchange").durable(true).build();
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
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                .to(notificationExchange())
                .with("notification.email.send");
    }

    @Bean
    public Binding inAppBinding() {
        return BindingBuilder.bind(inAppQueue())
                .to(notificationExchange())
                .with("notification.in-app.send");
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder.bind(pushQueue())
                .to(notificationExchange())
                .with("notification.push.send");
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
