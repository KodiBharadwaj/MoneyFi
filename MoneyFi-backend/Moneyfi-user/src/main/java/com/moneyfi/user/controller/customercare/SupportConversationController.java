package com.moneyfi.user.controller.customercare;

import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.service.secondary.SupportConversationService;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationsResponseDto;
import com.moneyfi.user.service.secondary.dto.response.SupportConversationResponse;
import com.moneyfi.user.util.enums.ConversationStatus;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-service/support/conversations")
public class SupportConversationController {

    private final UserAuthService userAuthService;
    private final SupportConversationService supportConversationService;

    @Operation(summary = "Api to initiate conversation", description = "User can initiate chat with customer care agent. Agent will be assigned here")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<SupportConversationResponse> startConversationWithAgent(Authentication authentication) {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return ResponseEntity.ok(supportConversationService.startConversationWithAgent(userId));
    }

    @Operation(summary = "Api to check a conversation", description = "Agent can find the list of assigned conversations and can work on it")
    @GetMapping(value = "/assigned")
    @PreAuthorize("hasRole('CUSTOMER_CARE')")
    public List<SupportConversationsResponseDto> fetchAgentAssignedConversations(Authentication authentication) {
        Long agentId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return supportConversationService.fetchAgentAssignedConversations(agentId);
    }

    @Operation(summary = "Api to change the conversation thread", description = "When agent opens a conversation, it gets marked to Waiting status")
    @GetMapping(value = "/{conversationId}/opened")
    @PreAuthorize("hasRole('CUSTOMER_CARE')")
    public void updateConversationThread(@NotBlank @PathVariable String conversationId,
                                         @NotNull @RequestParam ConversationStatus status) {
        supportConversationService.updateConversationThread(conversationId, status);
    }
}
