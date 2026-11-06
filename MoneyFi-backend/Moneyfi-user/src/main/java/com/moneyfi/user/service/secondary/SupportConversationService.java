package com.moneyfi.user.service.secondary;

import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationsResponseDto;
import com.moneyfi.user.util.enums.ConversationStatus;

import java.util.List;

public interface SupportConversationService {
    SupportConversationResponse startConversationWithAgent(Long userId);

    List<SupportConversationsResponseDto> fetchAgentAssignedConversations(Long agentId);

    void updateConversationThread(String conversationId, ConversationStatus status);
}
