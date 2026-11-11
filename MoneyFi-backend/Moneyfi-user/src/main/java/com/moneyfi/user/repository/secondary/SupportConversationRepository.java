package com.moneyfi.user.repository.secondary;

import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.util.enums.ConversationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportConversationRepository extends MongoRepository<SupportConversation, String> {

    List<SupportConversation> findAllByAgentId(Long agentId);

    List<SupportConversation> findAllByUserIdAndStatusIn(Long userId, Collection<ConversationStatus> statuses);

    Optional<SupportConversation> findFirstByIdAndUserId(String conversationId, Long userId);
}
