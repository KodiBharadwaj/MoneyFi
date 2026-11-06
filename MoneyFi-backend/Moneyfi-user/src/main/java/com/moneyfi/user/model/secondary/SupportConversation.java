package com.moneyfi.user.model.secondary;

import com.moneyfi.user.util.enums.ConversationStatus;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "support_conversations")
public class SupportConversation {

    @Id
    private String id;

    private Long userId;

    private Long agentId;

    private ConversationStatus status;

    private Instant assignedAt;

    private Instant startedAt;

    private Instant closedAt;

    private Instant lastMessageAt;

    private Long messageCount = 0L;

    private Instant createdAt;

    private Instant updatedAt;
}
