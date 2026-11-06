package com.moneyfi.user.controller.customercare;

import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.service.secondary.SupportChatService;
import com.moneyfi.user.service.secondary.dto.response.SendSupportMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Validated
@Controller
@RequiredArgsConstructor
public class SupportWebsocketChatController {

    private final UserAuthService userAuthService;
    private final SupportChatService supportChatService;

    @Operation(summary = "Api for websocket messaging", description = "User and Agent can chat with real time using websocket")
    @MessageMapping("/support/{conversationId}/send")
    public void sendMessage(@DestinationVariable String conversationId,
                            @Valid @Payload SendSupportMessageRequest request,
                            Authentication authentication) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        supportChatService.sendMessage(conversationId, request, userId);
    }
}
