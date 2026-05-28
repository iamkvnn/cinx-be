package com.cinx.enrollment.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.enrollment.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
