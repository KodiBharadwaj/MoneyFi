package com.moneyfi.user.service.secondary.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SupportConversationsResponseDto {
    private String conversationId;
    private Long userId;
    private Long agentId;
    private String customerName;
    private String status;
    private String lastMessage;
    private String lastMessageAt;
    private Integer messageCount;
}
