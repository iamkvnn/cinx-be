package com.cinx.course.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public TopicExchange enrollmentExchange() {
        return ExchangeBuilder.topicExchange("enrollment.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange courseExchange() {
        return ExchangeBuilder.topicExchange("course.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange aiSubtitleExchange() {
        return ExchangeBuilder.topicExchange("ai.subtitle.events.exchange").durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("dlx.exchange").durable(true).build();
    }

    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable("course.enrollment.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "course.enrollment.dead")
                .build();
    }

    @Bean
    public Queue enrollmentDeadLetterQueue() {
        return QueueBuilder.durable("course.enrollment.dead.queue").build();
    }

    @Bean
    public Queue subtitleAiQueue() {
        return QueueBuilder.durable("course.subtitle.ai.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "course.subtitle.ai.dead")
                .build();
    }

    @Bean
    public Queue subtitleAiDeadLetterQueue() {
        return QueueBuilder.durable("course.subtitle.ai.dead.queue").build();
    }

    @Bean
    public Binding enrollmentBinding() {
        return BindingBuilder.bind(enrollmentQueue())
                .to(enrollmentExchange())
                .with("enrollment.enrollment.created");
    }

    @Bean
    public Binding enrollmentDeadLetterBinding() {
        return BindingBuilder.bind(enrollmentDeadLetterQueue())
                .to(deadLetterExchange())
                .with("course.enrollment.dead");
    }

    @Bean
    public Binding subtitleAiProgressBinding() {
        return BindingBuilder.bind(subtitleAiQueue())
                .to(aiSubtitleExchange())
                .with("ai.subtitle.job.progress");
    }

    @Bean
    public Binding subtitleAiCompletedBinding() {
        return BindingBuilder.bind(subtitleAiQueue())
                .to(aiSubtitleExchange())
                .with("ai.subtitle.job.completed");
    }

    @Bean
    public Binding subtitleAiFailedBinding() {
        return BindingBuilder.bind(subtitleAiQueue())
                .to(aiSubtitleExchange())
                .with("ai.subtitle.job.failed");
    }

    @Bean
    public Binding subtitleAiDeadLetterBinding() {
        return BindingBuilder.bind(subtitleAiDeadLetterQueue())
                .to(deadLetterExchange())
                .with("course.subtitle.ai.dead");
    }
}
