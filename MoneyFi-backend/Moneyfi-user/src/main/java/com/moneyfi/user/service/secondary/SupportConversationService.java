package com.moneyfi.user.service.secondary;

import com.moneyfi.constants.enums.ActiveStatus;
import com.moneyfi.user.service.secondary.dto.response.ExistingConversationsResponseDto;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationsResponseDto;
import com.moneyfi.user.util.enums.ConversationStatus;

import java.util.List;

public interface SupportConversationService {
    SupportConversationResponse startConversationWithAgent(Long userId);

    SupportConversationResponse assignAgent(String conversationId, Long userId);

    List<ExistingConversationsResponseDto> getExistingConversations(Long userId, ActiveStatus status);

    List<SupportConversationsResponseDto> fetchAgentAssignedConversations(Long agentId);

    void updateConversationThread(String conversationId, ConversationStatus status);
}
