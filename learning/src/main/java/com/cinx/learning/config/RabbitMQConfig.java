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

    @Bean
    public Queue quizSyncQueue() {
        return QueueBuilder.durable("learning.sync-and-regrade.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "learning.sync-and-regrade.dead")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue quizSyncDeadLetterQueue() {
        return QueueBuilder.durable("learning.sync-and-regrade.dead.queue").build();
    }

    @Bean
    public Binding quizSyncBinding() {
        return BindingBuilder.bind(quizSyncQueue())
                .to(courseEventsExchange())
                .with("course.quiz.sync-and-regrade");
    }

    @Bean
    public Binding quizSyncDeadLetterBinding() {
        return BindingBuilder.bind(quizSyncDeadLetterQueue())
                .to(deadLetterExchange())
                .with("learning.sync-and-regrade.dead");
    }

    @Bean
    public Queue quizScoringModeChangeQueue() {
        return QueueBuilder.durable("learning.quiz.scoring-mode-change.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "learning.quiz.scoring-mode-change.dead")
                .withArgument("x-message-ttl", 60000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue quizScoringModeChangeDeadLetterQueue() {
        return QueueBuilder.durable("learning.quiz.scoring-mode-change.dead.queue").build();
    }

    @Bean
    public Binding quizScoringModeChangeBinding() {
        return BindingBuilder.bind(quizScoringModeChangeQueue())
                .to(courseEventsExchange())
                .with("course.quiz.scoring-mode-changed");
    }

    @Bean
    public Binding quizScoringModeChangeDeadLetterBinding() {
        return BindingBuilder.bind(quizScoringModeChangeDeadLetterQueue())
                .to(deadLetterExchange())
                .with("learning.quiz.scoring-mode-change.dead");
    }
}

