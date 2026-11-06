package com.moneyfi.user.service.secondary.dto.response;

import com.moneyfi.user.util.enums.MessageSenderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class SupportMessageResponse {
    private String messageId;
    private String conversationId;
    private Long senderId;
    private MessageSenderType senderType;
    private String content;
    private Instant sentAt;
}
