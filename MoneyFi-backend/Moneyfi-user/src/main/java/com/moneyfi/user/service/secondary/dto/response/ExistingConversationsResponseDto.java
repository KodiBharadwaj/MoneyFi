package com.moneyfi.user.service.secondary.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExistingConversationsResponseDto {
    private String conversationId;
    private String status;
}
