package com.cinx.social.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ReliablePublisherConfig {

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        // ...
        factory.setPublisherConfirmType(
                CachingConnectionFactory.ConfirmType.CORRELATED); // async confirms
        factory.setPublisherReturns(true); // catch unroutable messages
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        // Mandatory: get returned messages if unroutable
        template.setMandatory(true);
        template.setMessageConverter(jackson2JsonMessageConverter());

        // Handle publish confirmations
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("Message NACK'd by broker: {}", cause);
                // implement retry or save to outbox
            }
        });

        // Handle unroutable messages
        template.setReturnsCallback(returned -> {
            log.error("Message returned — exchange: {}, routingKey: {}, replyCode: {}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode());
        });

        return template;
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public MessageConverter messageConverter() {
        return jackson2JsonMessageConverter();
    }
}
