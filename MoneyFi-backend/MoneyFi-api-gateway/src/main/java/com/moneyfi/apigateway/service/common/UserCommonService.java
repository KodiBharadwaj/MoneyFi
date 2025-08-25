package com.moneyfi.apigateway.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;
import com.moneyfi.apigateway.service.common.dto.request.NameChangeRequestDto;
import com.moneyfi.apigateway.service.common.dto.response.QuoteResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserNotificationResponseDto;
import com.moneyfi.apigateway.service.common.dto.response.UserRequestStatusDto;

import java.util.List;
import java.util.Map;

public interface UserCommonService {

    String forgotPassword(String email);

    boolean verifyCode(String email, String code);

    String updatePassword(String email, String password);

    SessionTokenModel save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);

    BlackListedToken blacklistToken(BlackListedToken blackListedToken);

    boolean isTokenBlacklisted(String token);

    void accountUnblockRequestByUser(AccountRetrieveRequestDto requestDto);

    Map<Boolean, String> sendReferenceRequestNumberEmail(String requestStatus, String email);

    void nameChangeRequestByUser(NameChangeRequestDto requestDto);

    UserRequestStatusDto trackUserRequestUsingReferenceNumber(String referenceNumber);

    QuoteResponseDto getTodayQuoteByExternalCall(String externalApiUrl) throws JsonProcessingException;

    List<UserNotificationResponseDto> getUserNotifications(String username);

    Integer getUserNotificationsCount(String username);

    void updateUserNotificationSeenStatus(String username, String notificationIds);
}
