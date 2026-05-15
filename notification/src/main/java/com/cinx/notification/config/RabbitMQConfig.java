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
}
