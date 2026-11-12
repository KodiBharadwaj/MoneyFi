package com.moneyfi.user.model.secondary;

import com.moneyfi.user.util.enums.MessageSenderType;
import com.moneyfi.user.util.enums.MessageType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "support_messages")
public class SupportMessage {

    @Id
    private String id;

    private String conversationId;

    private Long senderId;

    private MessageSenderType senderType;

    private MessageType messageType;

    private boolean isRead;

    private String content;

    private Instant sentAt;

    private Instant readAt;
}
