package com.moneyfi.user.repository.secondary;

import com.moneyfi.user.model.secondary.SupportMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportMessageRepository extends MongoRepository<SupportMessage, String> {


    Optional<SupportMessage> findFirstBySenderIdOrderBySentAtDesc(Long senderId);

    long countByConversationId(String conversationId);

    List<SupportMessage> findAllByConversationIdOrderBySentAtAsc(String conversationId);
}
