package com.moneyfi.user.model.secondary;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder(toBuilder = true)
@Document(collection = "support_agent_workloads")
public class SupportAgentWorkload {

    @Id
    private String id;

    private Long agentId;

    private boolean online;

    private boolean available;

    private int activeConversationCount;

    private int maxConcurrentConversations;

    private Instant lastAssignedAt;

    private Instant updatedAt;
}
