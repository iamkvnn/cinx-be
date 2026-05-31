package com.cinx.enrollment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable("enrollment.payment.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "enrollment.payment.dead")
                .withArgument("x-max-length", 10000)           // backpressure
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("enrollment.payment.dead.queue").build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder.topicExchange("payment.events.exchange")
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
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange())
                .with("payment.payment.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("enrollment.payment.dead");
    }
}
