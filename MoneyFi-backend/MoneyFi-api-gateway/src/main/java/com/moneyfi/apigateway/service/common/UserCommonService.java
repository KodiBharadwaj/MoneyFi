package com.moneyfi.apigateway.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.service.common.dto.response.QuoteResponseDto;

public interface UserCommonService {

    String forgotPassword(String email);

    String verifyCode(String email, String code);

    String updatePasswordOnUserForgotMode(String email, String password);

    SessionTokenModel save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);

    BlackListedToken blacklistToken(BlackListedToken blackListedToken);

    boolean isTokenBlacklisted(String token);

    QuoteResponseDto getTodayQuoteByExternalCall(String externalApiUrl) throws JsonProcessingException;
}
