package com.cinx.payment.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.payment.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
