package com.moneyfi.apigateway.service.common;

import com.moneyfi.apigateway.model.auth.BlackListedToken;
import com.moneyfi.apigateway.model.auth.SessionTokenModel;

public interface UserCommonRepository {

    String forgotPassword(String email);

    boolean verifyCode(String email, String code);

    String UpdatePassword(String email,String password);

    SessionTokenModel save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);

    BlackListedToken blacklistToken(BlackListedToken blackListedToken);

    boolean isTokenBlacklisted(String token);
}
