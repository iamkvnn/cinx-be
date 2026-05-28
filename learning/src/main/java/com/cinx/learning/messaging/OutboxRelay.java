package com.cinx.learning.messaging;

import com.cinx.common.messaging.AbstractOutboxRelay;
import com.cinx.learning.model.OutboxMessage;
import com.cinx.learning.repository.OutboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelay extends AbstractOutboxRelay<OutboxMessage> {
    public OutboxRelay(OutboxMessageRepository outboxRepository,
                       RabbitTemplate rabbitTemplate,
                       ObjectMapper objectMapper) {
        super(outboxRepository, rabbitTemplate, objectMapper);
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay-delay-ms:5000}")
    public void relay() {
        publishPending();
    }
}
