package com.moneyfi.user.repository.secondary;

import com.moneyfi.user.model.secondary.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportMessageRepository extends MongoRepository<SupportMessage, String> {

    Page<SupportMessage> findByConversationIdOrderBySentAtAsc(String conversationId, Pageable pageable);
}
