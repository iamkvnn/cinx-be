package com.cinx.course.repository;

import com.cinx.common.messaging.OutboxRepository;
import com.cinx.course.model.OutboxMessage;

public interface OutboxMessageRepository extends OutboxRepository<OutboxMessage> {
}
