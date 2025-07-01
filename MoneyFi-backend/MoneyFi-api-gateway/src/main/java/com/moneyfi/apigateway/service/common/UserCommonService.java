package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.service.common.dto.request.AccountRetrieveRequestDto;

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

    Map<Boolean, String> accountUnblockRequestByUser(AccountRetrieveRequestDto requestDto);

    String sendReferenceRequestNumberEmail(String email);
}
