package com.cinx.social.config;

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
    public TopicExchange courseExchange() {
        return ExchangeBuilder.topicExchange("course.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange socialExchange() {
        return ExchangeBuilder.topicExchange("social.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange qnaExchange() {
        return ExchangeBuilder.topicExchange("course.qna.exchange").durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("dlx.exchange").durable(true).build();
    }

    @Bean
    public Queue courseArchivedQueue() {
        return QueueBuilder.durable("social.course-archived.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "social.course-archived.dead")
                .build();
    }

    @Bean
    public Queue courseArchivedDeadLetterQueue() {
        return QueueBuilder.durable("social.course-archived.dead.queue").build();
    }

    @Bean
    public Binding courseArchivedBinding() {
        return BindingBuilder.bind(courseArchivedQueue())
                .to(courseExchange())
                .with("course.course.archived");
    }

    @Bean
    public Binding courseArchivedDeadLetterBinding() {
        return BindingBuilder.bind(courseArchivedDeadLetterQueue())
                .to(deadLetterExchange())
                .with("social.course-archived.dead");
    }
}
