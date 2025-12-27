package com.moneyfi.apigateway.repository.gmailsync;

import com.moneyfi.apigateway.model.gmailsync.GmailProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GmailProcessedMessageRepository extends JpaRepository<GmailProcessedMessageEntity, Long> {

    boolean existsByMessageIdAndUserId(String messageId, Long userId);
}

