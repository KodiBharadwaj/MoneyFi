package com.moneyfi.apigateway.controller.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.apigateway.service.common.UserCommonService;
import com.moneyfi.apigateway.service.common.dto.response.QuoteResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.moneyfi.apigateway.util.constants.StringUtils.DAILY_QUOTE_EXTERNAL_API_URL;

@RestController
@RequestMapping("/api/v1/external-api")
public class ExternalApiCallsController {

    private final UserCommonService userCommonService;

    public ExternalApiCallsController(UserCommonService userCommonService){
        this.userCommonService = userCommonService;
    }

    @Operation(summary = "Api to make external api call from backend to get the daily quotes")
    @GetMapping("/get-quote/today")
    public QuoteResponseDto getTodayQuoteByExternalCall() throws JsonProcessingException {
        return userCommonService.getTodayQuoteByExternalCall(DAILY_QUOTE_EXTERNAL_API_URL);
    }
}
