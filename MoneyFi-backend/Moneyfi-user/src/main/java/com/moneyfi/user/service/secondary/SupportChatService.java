package com.moneyfi.user.service.secondary;

import com.moneyfi.user.service.secondary.dto.response.SendSupportMessageRequest;
import com.moneyfi.user.service.secondary.dto.response.SupportMessagesChatDto;

import java.util.List;

public interface SupportChatService {
    void sendMessage(String conversationId, SendSupportMessageRequest request, Long userId);

    List<SupportMessagesChatDto> fetchChatHistory(String conversationId);
}
