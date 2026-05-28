package com.cinx.common.messaging;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface OutboxRepository<T extends OutboxMessageBase> extends JpaRepository<T, String> {
    boolean existsByEventId(String eventId);

    List<T> findByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            LocalDateTime now,
            Pageable pageable
    );
}
