package com.moneyfi.user.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.user.UserAuthService;
import com.moneyfi.user.service.user.UserCommonService;
import com.moneyfi.user.service.user.dto.response.QuoteResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-service/external-api")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ExternalApiCallsController {

    private final UserCommonService userCommonService;
    private final UserAuthService userAuthService;

    @Operation(summary = "Api to make external api call from backend to get the daily quotes")
    @GetMapping("/get-quote/today")
    public QuoteResponseDto getTodayQuoteByExternalCall(Authentication authentication) throws JsonProcessingException {
        Long userId = userAuthService.getUserIdByUsername(((UserDetails) authentication.getPrincipal()).getUsername());
        return userCommonService.getTodayQuoteByExternalCall(userId);
    }
}
