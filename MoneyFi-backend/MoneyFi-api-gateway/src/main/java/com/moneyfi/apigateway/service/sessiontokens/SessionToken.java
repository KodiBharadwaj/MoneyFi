package com.moneyfi.apigateway.service.sessiontokens;

import com.moneyfi.apigateway.model.auth.SessionTokenModel;

public interface SessionToken {

    SessionTokenModel save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);
}
