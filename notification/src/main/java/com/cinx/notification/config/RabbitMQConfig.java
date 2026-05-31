package com.cinx.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange paymentExchange() {
        return ExchangeBuilder.topicExchange("payment.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange courseExchange() {
        return ExchangeBuilder.topicExchange("course.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange qnaExchange() {
        return ExchangeBuilder.topicExchange("course.qna.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange learningExchange() {
        return ExchangeBuilder.topicExchange("learning.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange orderExchange() {
        return ExchangeBuilder.topicExchange("order.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange authExchange() {
        return ExchangeBuilder.topicExchange("auth.events.exchange").durable(true).build();
    }

    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder.topicExchange("user.events.exchange").durable(true).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("dlx.exchange").durable(true).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("notification.dlq").build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable("notification.payment.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "payment.dead")
                .build();
    }

    @Bean
    public Queue courseQueue() {
        return QueueBuilder.durable("notification.course.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "course.dead")
                .build();
    }

    @Bean
    public Queue socialQueue() {
        return QueueBuilder.durable("notification.social.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "social.dead")
                .build();
    }

    @Bean
    public Queue learningQueue() {
        return QueueBuilder.durable("notification.learning.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "learning.dead")
                .build();
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("notification.order.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "order.dead")
                .build();
    }

    @Bean
    public Queue authQueue() {
        return QueueBuilder.durable("notification.auth.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "auth.dead")
                .build();
    }

    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable("notification.user.queue")
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .withArgument("x-dead-letter-routing-key", "user.dead")
                .build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(paymentExchange())
                .with("payment.payment.success");
    }

    @Bean
    public Binding courseLessonChangedBinding() {
        return BindingBuilder.bind(courseQueue())
                .to(courseExchange())
                .with("course.lesson.changed");
    }

    @Bean
    public Binding qnaQuestionCreatedBinding() {
        return BindingBuilder.bind(socialQueue())
                .to(qnaExchange())
                .with("question.created");
    }

    @Bean
    public Binding qnaAnswerCreatedBinding() {
        return BindingBuilder.bind(socialQueue())
                .to(qnaExchange())
                .with("answer.created");
    }

    @Bean
    public Binding learningCourseCompletedBinding() {
        return BindingBuilder.bind(learningQueue())
                .to(learningExchange())
                .with("learning.course.completed");
    }

    @Bean
    public Binding learningReminderDueBinding() {
        return BindingBuilder.bind(learningQueue())
                .to(learningExchange())
                .with("learning.reminder.due");
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with("order.order.created");
    }

    // auth.events.exchange — wildcard catches all OTP and account lifecycle events
    @Bean
    public Binding authEventsBinding() {
        return BindingBuilder.bind(authQueue())
                .to(authExchange())
                .with("auth.#");
    }

    // user.events.exchange — wildcard catches instructor and future user events
    @Bean
    public Binding userEventsBinding() {
        return BindingBuilder.bind(userQueue())
                .to(userExchange())
                .with("user.#");
    }

    @Bean
    public Binding dlqPaymentBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("payment.dead");
    }

    @Bean
    public Binding dlqCourseBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("course.dead");
    }

    @Bean
    public Binding dlqSocialBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("social.dead");
    }

    @Bean
    public Binding dlqLearningBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("learning.dead");
    }

    @Bean
    public Binding dlqOrderBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("order.dead");
    }

    @Bean
    public Binding dlqAuthBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("auth.dead");
    }

    @Bean
    public Binding dlqUserBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("user.dead");
    }
}
