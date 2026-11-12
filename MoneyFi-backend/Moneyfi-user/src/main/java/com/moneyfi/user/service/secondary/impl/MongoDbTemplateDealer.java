package com.moneyfi.user.service.secondary.impl;

import com.moneyfi.user.model.secondary.SupportAgentWorkload;
import com.moneyfi.user.model.secondary.SupportConversation;
import com.moneyfi.user.model.secondary.SupportMessage;
import com.moneyfi.user.service.secondary.dto.response.SupportMessageResponse;
import com.moneyfi.user.util.enums.ConversationStatus;
import com.moneyfi.user.util.enums.MessageSenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.moneyfi.constants.constants.BasePathConstants.USER_SERVICE_BASE_PATH;

@Service
@RequiredArgsConstructor
public class MongoDbTemplateDealer {

    private final MongoTemplate mongoTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public static final String ACTIVE_CONVERSATION_COUNT = "activeConversationCount";
    public static final String LAST_ASSIGNED_AT = "lastAssignedAt";
    public static final String ID = "_id";
    public static final String UPDATED_AT = "updatedAt";
    public static final String STATUS = "status";
    public static final String MESSAGE_COUNT = "messageCount";
    public static final String LAST_MESSAGE_AT = "lastMessageAt";

    public SupportAgentWorkload assignAgent() {
        Query query = new BasicQuery("""
                {
                    "online": true,
                    "available": true,
                    "$expr": {
                        "$lt": [
                            "$activeConversationCount",
                            "$maxConcurrentConversations"
                        ]
                    }
                }
                """);
        query.with(Sort.by(
                Sort.Order.asc(ACTIVE_CONVERSATION_COUNT),
                Sort.Order.asc(LAST_ASSIGNED_AT),
                Sort.Order.asc(ID)
        ));

        Update update = new Update()
                .inc(ACTIVE_CONVERSATION_COUNT, 1)
                .set(LAST_ASSIGNED_AT, Instant.now())
                .set(UPDATED_AT, Instant.now());

        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), SupportAgentWorkload.class);
    }

    public void releaseAgent(Long agentId) {
        Query query = Query.query(
                org.springframework.data.mongodb.core.query.Criteria
                        .where(ID).is(agentId)
                        .and(ACTIVE_CONVERSATION_COUNT).gt(0)
        );

        Update update = new Update()
                .inc(ACTIVE_CONVERSATION_COUNT, -1)
                .set(UPDATED_AT, Instant.now());

        mongoTemplate.updateFirst(query, update, SupportAgentWorkload.class);
    }

    public void updateConversationAndSendMessageToWebsocketUsingTopic(Instant currentInstantTime, Long userId, SupportMessage savedMessage, String conversationId, MessageSenderType senderType) {
        Query query = Query.query(
                Criteria.where(ID).is(conversationId)
                        .and(STATUS).is(ConversationStatus.ACTIVE)
        );
        Update update = new Update()
                .inc(MESSAGE_COUNT, 1)
                .set(LAST_MESSAGE_AT, currentInstantTime)
                .set(UPDATED_AT, currentInstantTime);
        mongoTemplate.updateFirst(query, update, SupportConversation.class);

        SupportMessageResponse response =
                SupportMessageResponse.builder()
                        .messageId(savedMessage.getId())
                        .conversationId(conversationId)
                        .senderId(userId)
                        .senderType(senderType)
                        .content(savedMessage.getContent())
                        .sentAt(currentInstantTime)
                        .build();
        messagingTemplate.convertAndSend(USER_SERVICE_BASE_PATH + "/topic/support/" + conversationId, response);
    }
}
