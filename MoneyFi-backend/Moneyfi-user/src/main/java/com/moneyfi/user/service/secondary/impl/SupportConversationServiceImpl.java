package com.moneyfi.user.service.secondary.impl;

import com.moneyfi.constants.enums.ActiveStatus;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.exceptions.ScenarioNotPossibleException;
import com.moneyfi.user.model.primary.general.ProfileModel;
import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.model.secondary.SupportMessage;
import com.moneyfi.user.repository.primary.general.ProfileRepository;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.repository.secondary.SupportMessageRepository;
import com.moneyfi.user.service.secondary.SupportConversationService;
import com.moneyfi.user.service.secondary.dto.response.ExistingConversationsResponseDto;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationsResponseDto;
import com.moneyfi.user.util.enums.ConversationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.moneyfi.user.util.constants.StringConstants.CONVERSATION_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportConversationServiceImpl implements SupportConversationService {

    private final SupportConversationRepository conversationRepository;
    private final MongoDbTemplateDealer assignmentService;
    private final ProfileRepository profileRepository;
    private final SupportMessageRepository supportMessageRepository;

    private static final String CUSTOMER_CARE_EXECUTIVE_ASSIGNED_SUCCESSFULLY = "Customer care executive assigned successfully";
    private static final String YOU_ARE_IN_THE_WAITING_QUEUE = "You are in the waiting queue";
    private static final String AGENTS_ARE_NOT_AVAILABLE = "Agents are not available";
    private static final String AGENT_ALREADY_ASSIGNED = "Agent already assigned";

    @Override
    public SupportConversationResponse startConversationWithAgent(Long userId) {
        SupportAgentWorkload agent = assignmentService.assignAgent();
        boolean isAgentAssigned = agent != null;

        Instant currentInstantTime = Instant.now();
        SupportConversation conversation = SupportConversation.builder()
                .userId(userId)
                .agentId(isAgentAssigned ? agent.getAgentId() : null)
                .status(isAgentAssigned ? ConversationStatus.ACTIVE : ConversationStatus.WAITING)
                .assignedAt(isAgentAssigned ? currentInstantTime : null)
                .startedAt(isAgentAssigned ? currentInstantTime : null)
                .createdAt(currentInstantTime)
                .updatedAt(currentInstantTime)
                .build();
        return getSupportConversationResponse(conversation, isAgentAssigned, isAgentAssigned ? CUSTOMER_CARE_EXECUTIVE_ASSIGNED_SUCCESSFULLY : YOU_ARE_IN_THE_WAITING_QUEUE, agent);
    }

    @Override
    public SupportConversationResponse assignAgent(String conversationId, Long userId) {
        Optional<SupportConversation> existingConversation = conversationRepository.findFirstByIdAndUserId(conversationId, userId);
        if (existingConversation.isEmpty()) throw new ScenarioNotPossibleException(CONVERSATION_NOT_FOUND);
        SupportConversation conversation = existingConversation.get();
        if (conversation.getAgentId() != null) throw new ScenarioNotPossibleException(AGENT_ALREADY_ASSIGNED);

        SupportAgentWorkload agent = assignmentService.assignAgent();
        boolean isAgentAssigned = agent != null;

        Instant currentInstantTime = Instant.now();
        SupportConversation updatedConversation = conversation.toBuilder()
                .agentId(isAgentAssigned ? agent.getAgentId() : null)
                .status(isAgentAssigned ? ConversationStatus.ACTIVE : ConversationStatus.WAITING)
                .assignedAt(isAgentAssigned ? currentInstantTime : null)
                .updatedAt(currentInstantTime)
                .build();
        return getSupportConversationResponse(updatedConversation, isAgentAssigned, isAgentAssigned ? CUSTOMER_CARE_EXECUTIVE_ASSIGNED_SUCCESSFULLY : AGENTS_ARE_NOT_AVAILABLE, agent);
    }

    @Override
    public List<ExistingConversationsResponseDto> getExistingConversations(Long userId, ActiveStatus status) {
        Set<ConversationStatus> statuses = switch (status) {
            case ACTIVE -> EnumSet.of(ConversationStatus.ACTIVE, ConversationStatus.WAITING, ConversationStatus.IN_PROGRESS);
            case INACTIVE -> EnumSet.of(ConversationStatus.CLOSED, ConversationStatus.CANCELLED);
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };

        return conversationRepository.findAllByUserIdAndStatusIn(userId, statuses).stream()
                .map(conversation -> ExistingConversationsResponseDto.builder()
                        .conversationId(conversation.getId())
                        .status(conversation.getStatus().name())
                        .build())
                .toList();
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConversationThread(String conversationId, ConversationStatus status) {
        SupportConversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));
        Instant currentInstant = Instant.now();
        SupportConversation.SupportConversationBuilder updatedBuilder = conversation.toBuilder()
                .status(status)
                .updatedAt(currentInstant);
        if (status == ConversationStatus.CANCELLED || status == ConversationStatus.CLOSED) {
            updatedBuilder.updatedAt(currentInstant);
            updatedBuilder.closedAt(currentInstant);
        }
        conversationRepository.save(updatedBuilder.build());
    }

    private SupportConversationResponse getSupportConversationResponse(SupportConversation conversation, boolean isAgentAssigned, String statusMessage, SupportAgentWorkload agent) {
        try {
            return mapSupportConversationResponse(
                    conversationRepository.save(conversation), statusMessage
            );
        } catch (RuntimeException exception) {
            log.error("Exception occurred: {}", exception.getMessage());
            if (isAgentAssigned) assignmentService.releaseAgent(agent.getAgentId());
            throw exception;
        }
    }

    private SupportConversationResponse mapSupportConversationResponse(SupportConversation conversation, String message) {
        return SupportConversationResponse.builder()
                .conversationId(conversation.getId())
                .userId(conversation.getUserId())
                .agentId(conversation.getAgentId())
                .status(conversation.getStatus())
                .message(message)
                .build();
    }
}
