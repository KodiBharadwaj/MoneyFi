package com.moneyfi.user.service.secondary.dto.response;

import com.moneyfi.user.util.enums.ConversationStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportConversationResponse {
    private String conversationId;
    private Long userId;
    private Long agentId;
    private ConversationStatus status;
    private String message;
}
