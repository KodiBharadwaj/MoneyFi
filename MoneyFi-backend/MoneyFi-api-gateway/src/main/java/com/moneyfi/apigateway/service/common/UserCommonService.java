package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;

public interface UserCommonService {

    String forgotPassword(String email);

    String verifyCode(String email, String code);

    String updatePasswordOnUserForgotMode(String email, String password);

    SessionTokenModel save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);

    BlackListedToken blacklistToken(BlackListedToken blackListedToken);

    boolean isTokenBlacklisted(String token, String username);
}
