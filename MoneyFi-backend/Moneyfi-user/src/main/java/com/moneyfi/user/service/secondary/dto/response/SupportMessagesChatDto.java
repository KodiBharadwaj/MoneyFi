package com.moneyfi.user.service.secondary.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SupportMessagesChatDto {
    private String messageId;
    private String conversationId;
    private Long senderId;
    private String senderType;
    private String content;
    private String sentAt;
}
