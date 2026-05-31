package com.cinx.payment.messaging;

import com.cinx.common.messaging.AbstractOutboxEventPublisher;
import com.cinx.payment.model.OutboxMessage;
import com.cinx.payment.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class OutboxEventPublisher extends AbstractOutboxEventPublisher<OutboxMessage> {
    public OutboxEventPublisher(OutboxMessageRepository outboxRepository, ObjectMapper objectMapper) {
        super(outboxRepository, objectMapper);
    }

    @Override
    protected OutboxMessage newMessage() {
        return new OutboxMessage();
    }
}
