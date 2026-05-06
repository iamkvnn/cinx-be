package com.cinx.learning.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable("learning.enrollment.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "learning.enrollment.dead")
                .withArgument("x-message-ttl", 60000)          // 60s TTL
                .withArgument("x-max-length", 10000)           // backpressure
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("learning.enrollment.dead.queue").build();
    }

    @Bean
    public TopicExchange enrollmentExchange() {
        return ExchangeBuilder.topicExchange("enrollment.events.exchange")
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
    public Binding enrollmentBinding() {
        return BindingBuilder.bind(enrollmentQueue())
                .to(enrollmentExchange())
                .with("enrollment.enrollment.#");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("learning.enrollment.dead");
    }

    @Bean
    public TopicExchange learningEventsExchange() {
        return ExchangeBuilder.topicExchange("learning.events.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public Queue lessonChangeQueue() {
        return QueueBuilder.durable("learning.lesson-change.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "learning.lesson-change.dead")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue lessonChangeDeadLetterQueue() {
        return QueueBuilder.durable("learning.lesson-change.dead.queue").build();
    }

    @Bean
    public TopicExchange courseEventsExchange() {
        return ExchangeBuilder.topicExchange("course.events.exchange")
                .durable(true)
                .build();
    }

    @Bean
    public Binding lessonChangeBinding() {
        return BindingBuilder.bind(lessonChangeQueue())
                .to(courseEventsExchange())
                .with("course.lesson.#");
    }

    @Bean
    public Binding lessonChangeDeadLetterBinding() {
        return BindingBuilder.bind(lessonChangeDeadLetterQueue())
                .to(deadLetterExchange())
                .with("learning.lesson-change.dead");
    }
}
