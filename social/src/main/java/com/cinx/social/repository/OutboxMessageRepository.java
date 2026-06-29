package com.cinx.social.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.social.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
