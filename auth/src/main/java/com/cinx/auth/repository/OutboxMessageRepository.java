package com.cinx.auth.repository;

import com.cinx.auth.model.OutboxMessage;
import com.cinx.common.messaging.OutboxRepository;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
