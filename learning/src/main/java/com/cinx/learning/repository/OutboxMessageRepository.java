package com.cinx.learning.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.learning.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
