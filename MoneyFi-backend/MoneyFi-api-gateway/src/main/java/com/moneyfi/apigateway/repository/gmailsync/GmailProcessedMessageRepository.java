package com.moneyfi.apigateway.repository.gmailsync;

import com.moneyfi.apigateway.model.gmailsync.GmailProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GmailProcessedMessageRepository extends JpaRepository<GmailProcessedMessageEntity, Long> {

    Optional<GmailProcessedMessageEntity> findByMessageIdAndUserId(String messageId, Long userId);

    Optional<GmailProcessedMessageEntity> findByMessageId(String messageId);
}

