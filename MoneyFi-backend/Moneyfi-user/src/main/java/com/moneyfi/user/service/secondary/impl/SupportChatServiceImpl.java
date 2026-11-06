package com.moneyfi.user.service.secondary.impl;

import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.model.secondary.SupportMessage;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.repository.secondary.SupportMessageRepository;
import com.moneyfi.user.service.secondary.SupportChatService;
import com.moneyfi.user.service.secondary.dto.response.SendSupportMessageRequest;
import com.moneyfi.user.service.secondary.dto.response.SupportMessagesChatDto;
import com.moneyfi.user.util.enums.ConversationStatus;
import com.moneyfi.user.util.enums.MessageSenderType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportChatServiceImpl implements SupportChatService {

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final MongoDbTemplateDealer mongoDbTemplateDealer;

    public static final String NOT_PART_OF_THIS_CONVERSATION = "You are not part of this conversation";
    public static final String CONVERSATION_NOT_FOUND = "Conversation not found";
    public static final String CONVERSATION_IS_NOT_ACTIVE = "Conversation is not active";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String conversationId, SendSupportMessageRequest request, Long userId) {
        SupportConversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException(CONVERSATION_NOT_FOUND));

        if (conversation.getStatus() != ConversationStatus.ACTIVE)
            throw new IllegalStateException(CONVERSATION_IS_NOT_ACTIVE);

        MessageSenderType senderType;
        if (userId.equals(conversation.getUserId()))
            senderType = MessageSenderType.USER;
        else if (userId.equals(conversation.getAgentId()))
            senderType = MessageSenderType.AGENT;
        else throw new AccessDeniedException(NOT_PART_OF_THIS_CONVERSATION);

        Instant currentInstantTime = Instant.now();
        SupportMessage message = SupportMessage.builder()
                .conversationId(conversationId)
                .senderId(userId)
                .senderType(senderType)
                .content(request.content().trim())
                .sentAt(currentInstantTime)
                .build();
        SupportMessage savedMessage = messageRepository.save(message);
        mongoDbTemplateDealer.updateConversationAndSendMessageToWebsocketUsingTopic(currentInstantTime, userId, savedMessage, conversationId, senderType);
    }

    @Override
    public List<SupportMessagesChatDto> fetchChatHistory(String conversationId) {
        return messageRepository.findAllByConversationIdOrderBySentAtAsc(conversationId).stream()
                .map(chatHistoryRecord ->
                        SupportMessagesChatDto.builder()
                                .messageId(chatHistoryRecord.getId())
                                .conversationId(chatHistoryRecord.getConversationId())
                                .senderId(chatHistoryRecord.getSenderId())
                                .senderType(StringUtils.capitalize(chatHistoryRecord.getSenderType().name()))
                                .content(chatHistoryRecord.getContent())
                                .sentAt(chatHistoryRecord.getSentAt().toString())
                                .build()
                )
                .toList();
    }
}
