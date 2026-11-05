package com.moneyfi.user.service.secondary;

import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.util.enums.ConversationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportConversationService {

    private final SupportConversationRepository conversationRepository;
    private final SupportAgentAssignmentService assignmentService;

    private static final List<ConversationStatus> OPEN_STATUSES =
            List.of(
                    ConversationStatus.WAITING,
                    ConversationStatus.ACTIVE
            );

    public SupportConversationResponse startConversation(String userId) {

        // 1. Check whether the user already has an open conversation.
        Optional<SupportConversation> existing = conversationRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(userId, OPEN_STATUSES);

        if (existing.isPresent()) return mapResponse(existing.get(), "Existing support conversation retrieved");

        // 2. Atomically reserve an available agent.
        SupportAgentWorkload agent = assignmentService.assignAgent();

        Instant now = Instant.now();

        // 3. Create the conversation.
        SupportConversation conversation = SupportConversation.builder()
                .userId(userId)
                .agentId(agent != null ? agent.getAgentId() : null)
                .status(agent != null
                        ? ConversationStatus.ACTIVE
                        : ConversationStatus.WAITING)
                .assignedAt(agent != null ? now : null)
                .startedAt(agent != null ? now : null)
                .messageCount(0L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            SupportConversation saved = conversationRepository.save(conversation);

            return mapResponse(
                    saved,
                    agent != null
                            ? "Customer care executive assigned successfully"
                            : "You are in the waiting queue"
            );

        } catch (RuntimeException exception) {

            // Compensate for a reserved workload if persistence fails.
            if (agent != null) {
                assignmentService.releaseAgent(agent.getAgentId());
            }

            throw exception;
        }
    }

    private SupportConversationResponse mapResponse(SupportConversation conversation, String message) {
        return SupportConversationResponse.builder()
                .conversationId(conversation.getId())
                .userId(conversation.getUserId())
                .agentId(conversation.getAgentId())
                .status(conversation.getStatus())
                .message(message)
                .build();
    }
}
