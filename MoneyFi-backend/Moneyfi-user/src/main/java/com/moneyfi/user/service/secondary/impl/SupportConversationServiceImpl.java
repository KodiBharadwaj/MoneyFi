package com.moneyfi.user.service.secondary.impl;

import com.moneyfi.user.model.primary.general.ProfileModel;
import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.model.secondary.SupportMessage;
import com.moneyfi.user.repository.primary.general.ProfileRepository;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.repository.secondary.SupportMessageRepository;
import com.moneyfi.user.service.secondary.SupportConversationService;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationsResponseDto;
import com.moneyfi.user.util.enums.ConversationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportConversationServiceImpl implements SupportConversationService {

    private final SupportConversationRepository conversationRepository;
    private final MongoDbTemplateDealer assignmentService;
    private final ProfileRepository profileRepository;
    private final SupportMessageRepository supportMessageRepository;

    private static final List<ConversationStatus> OPEN_STATUSES = List.of(ConversationStatus.WAITING, ConversationStatus.ACTIVE);

    public SupportConversationResponse startConversationWithAgent(Long userId) {

        Optional<SupportConversation> existing =
                conversationRepository.findFirstByUserIdAndStatusInOrderByCreatedAtDesc(userId, OPEN_STATUSES);

        if (existing.isPresent()) return mapResponse(existing.get(), "Existing support conversation retrieved");

        SupportAgentWorkload agent = assignmentService.assignAgent();

        Instant currentInstantTime = Instant.now();
        SupportConversation conversation = SupportConversation.builder()
                .userId(userId)
                .agentId(agent != null ? agent.getAgentId() : null)
                .status(agent != null ? ConversationStatus.ACTIVE : ConversationStatus.WAITING)
                .assignedAt(agent != null ? currentInstantTime : null)
                .startedAt(agent != null ? currentInstantTime : null)
                .messageCount(0L)
                .createdAt(currentInstantTime)
                .updatedAt(currentInstantTime)
                .build();

        try {
            return mapResponse(
                    conversationRepository.save(conversation),
                    agent != null ? "Customer care executive assigned successfully" : "You are in the waiting queue"
            );
        } catch (RuntimeException exception) {
            log.error("Exception occurred: {}", exception.getMessage());
            if (agent != null) {
                assignmentService.releaseAgent(agent.getAgentId());
            }
            throw exception;
        }
    }

    @Override
    public List<SupportConversationsResponseDto> fetchAgentAssignedConversations(Long agentId) {
        return conversationRepository.findAllByAgentId(agentId).stream()
                .map(assignedConversation -> {
                    Optional<SupportMessage> messageDetails = supportMessageRepository.findFirstBySenderIdOrderBySentAtDesc(assignedConversation.getUserId());
                    return SupportConversationsResponseDto.builder()
                            .conversationId(assignedConversation.getId())
                            .userId(assignedConversation.getUserId())
                            .agentId(assignedConversation.getAgentId())
                            .customerName(profileRepository.findByUserId(assignedConversation.getUserId()).map(ProfileModel::getName).orElse("Unknown"))
                            .status(StringUtils.capitalize(assignedConversation.getStatus().name()))
                            .lastMessage(messageDetails.map(SupportMessage::getContent).orElse("Not found"))
                            .lastMessageAt(messageDetails.map(SupportMessage::getSentAt).orElse(Instant.now()).toString())
                            .messageCount((int) supportMessageRepository.countByConversationId(assignedConversation.getId()))
                            .build();
                })
                .toList();
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
