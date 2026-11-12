package com.moneyfi.user.controller.common;

import com.moneyfi.user.service.primary.user.UserAuthService;
import com.moneyfi.user.service.secondary.SupportChatService;
import com.moneyfi.user.service.secondary.dto.response.SupportMessagesChatDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.moneyfi.constants.constants.CommonConstants.AUTHORIZATION;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
public class CommonController {

    private final UserAuthService userAuthService;
    private final SupportChatService supportChatService;

    @Operation(summary = "Api to logout/making the token blacklist")
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MAINTAINER','CUSTOMER_CARE')")
    public ResponseEntity<Map<String, String>> logoutUser(@RequestHeader(AUTHORIZATION) String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok(userAuthService.logout(token));
    }

    @Operation(summary = "Api to increase otp expiration time on user request")
    @GetMapping("/extend-session")
    @PreAuthorize("hasAnyRole('USER','ADMIN','CUSTOMER_CARE','MAINTAINER')")
    public ResponseEntity<String> updateUserSessionExpirationTime(Authentication authentication,
                                                                  @RequestHeader(AUTHORIZATION) String authHeader,
                                                                  @NotNull @RequestParam long minutes) {
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(userAuthService.updateUserSessionExpirationTime(minutes, username, authHeader.substring(7)));
    }

    @Operation(summary = "Api to get the username from token and simply return")
    @GetMapping("/get-username")
    @PreAuthorize("hasAnyRole('USER','ADMIN','CUSTOMER_CARE')")
    public ResponseEntity<String> getUsername(Authentication authentication){
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(username);
    }

    @Operation(summary = "Api to fetch messages", description = "End point to fetch the chat history")
    @GetMapping(value = "/conversations/{conversationId}/messages")
    @PreAuthorize("hasAnyRole('USER','CUSTOMER_CARE')")
    public ResponseEntity<List<SupportMessagesChatDto>> fetchChatHistory(@PathVariable String conversationId) {
        return ResponseEntity.ok(supportChatService.fetchChatHistory(conversationId));
    }
}
