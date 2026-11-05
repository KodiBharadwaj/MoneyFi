package com.moneyfi.user.controller.customercare;

import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.service.secondary.SupportConversationService;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-service/support/conversations")
@RequiredArgsConstructor
public class SupportConversationController {

    private final SupportConversationService conversationService;
    private final UserAuthService userAuthService;

    @PostMapping
    public ResponseEntity<SupportConversationResponse> startConversation(Authentication authentication) {

        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        SupportConversationResponse response = conversationService.startConversation(userId.toString());
        return ResponseEntity.ok(response);
    }
}
