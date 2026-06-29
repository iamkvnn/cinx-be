package com.cinx.user.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.user.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
