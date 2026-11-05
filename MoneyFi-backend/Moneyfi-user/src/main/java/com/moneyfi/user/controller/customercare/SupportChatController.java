package com.moneyfi.user.controller.customercare;

import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.model.secondary.SupportMessage;
import com.moneyfi.user.repository.secondary.SupportConversationRepository;
import com.moneyfi.user.repository.secondary.SupportMessageRepository;
import com.moneyfi.user.service.secondary.dto.response.SendSupportMessageRequest;
import com.moneyfi.user.service.secondary.dto.response.SupportMessageResponse;
import com.moneyfi.user.util.enums.ConversationStatus;
import com.moneyfi.user.util.enums.MessageSenderType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class SupportChatController {

    private final SupportConversationRepository conversationRepository;
    private final SupportMessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/support/{conversationId}/send")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Valid @Payload SendSupportMessageRequest request,
            Principal principal) {

        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated sender");
        }

        // 1. Find conversation
        SupportConversation conversation =
                conversationRepository.findById(conversationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Conversation not found"
                                ));

        // 2. Validate conversation status
        if (conversation.getStatus() != ConversationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Conversation is not active"
            );
        }

        // 3. Identify sender from authenticated principal
        String senderId = principal.getName();

        MessageSenderType senderType;

        if (senderId.equals(conversation.getUserId())) {
            senderType = MessageSenderType.USER;

        } else if (senderId.equals(conversation.getAgentId())) {
            senderType = MessageSenderType.AGENT;

        } else {
            throw new AccessDeniedException(
                    "You are not part of this conversation"
            );
        }

        // 4. Persist message
        Instant now = Instant.now();

        SupportMessage message = SupportMessage.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .senderType(senderType)
                .content(request.content().trim())
                .sentAt(now)
                .build();

        SupportMessage savedMessage =
                messageRepository.save(message);

        // 5. Update conversation metadata
        Query query = Query.query(
                Criteria.where("_id").is(conversationId)
                        .and("status").is(ConversationStatus.ACTIVE)
        );

        Update update = new Update()
                .inc("messageCount", 1)
                .set("lastMessageAt", now)
                .set("updatedAt", now);

        mongoTemplate.updateFirst(
                query,
                update,
                SupportConversation.class
        );

        // 6. Broadcast to both participants
        SupportMessageResponse response =
                SupportMessageResponse.builder()
                        .messageId(savedMessage.getId())
                        .conversationId(conversationId)
                        .senderId(senderId)
                        .senderType(senderType)
                        .content(savedMessage.getContent())
                        .sentAt(now)
                        .build();

        messagingTemplate.convertAndSend(
                "/topic/support/" + conversationId,
                response
        );
    }
}
