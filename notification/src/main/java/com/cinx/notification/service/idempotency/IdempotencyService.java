package com.cinx.notification.service.idempotency;

import com.cinx.notification.model.InboxMessage;
import com.cinx.notification.repository.InboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final InboxMessageRepository inboxMessageRepository;
    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean checkAndSave(String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            log.warn("Received message without ID, allowing it to process (could lead to duplicates)");
            return true;
        }

        try {
            InboxMessage inboxMessage = InboxMessage.builder()
                    .messageId(messageId)
                    .status("SUCCESS")
                    .processedAt(LocalDateTime.now())
                    .build();
            inboxMessageRepository.saveAndFlush(inboxMessage);
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Message with ID {} already processed (Constraint Violation), skipping.", messageId);
            return false;
        }
    }
}
