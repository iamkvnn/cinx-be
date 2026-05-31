package com.cinx.common.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public abstract class AbstractOutboxEventPublisher<T extends OutboxMessageBase> {
    private final OutboxRepository<T> outboxRepository;
    private final ObjectMapper objectMapper;

    protected AbstractOutboxEventPublisher(OutboxRepository<T> outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    protected abstract T newMessage();

    @Transactional
    public void enqueue(String eventId,
                        String aggregateType,
                        String aggregateId,
                        String eventType,
                        String exchange,
                        String routingKey,
                        Object payload) {
        enqueue(eventId, aggregateType, aggregateId, eventType, exchange, routingKey, payload, Map.of());
    }

    @Transactional
    public void enqueue(String eventId,
                        String aggregateType,
                        String aggregateId,
                        String eventType,
                        String exchange,
                        String routingKey,
                        Object payload,
                        Map<String, Object> headers) {
        if (outboxRepository.existsByEventId(eventId)) {
            return;
        }
        T message = newMessage();
        message.setEventId(eventId);
        message.setAggregateType(aggregateType);
        message.setAggregateId(aggregateId);
        message.setEventType(eventType);
        message.setEventVersion(1);
        message.setExchangeName(exchange);
        message.setRoutingKey(routingKey);
        message.setStatus(OutboxStatus.PENDING);
        message.setAttempts(0);
        try {
            message.setPayloadJson(objectMapper.writeValueAsString(payload));
            message.setHeadersJson(objectMapper.writeValueAsString(headers));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event", e);
        }
        outboxRepository.save(message);
    }
}
