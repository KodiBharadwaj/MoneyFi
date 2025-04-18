package com.moneyfi.apigateway.service.sessiontokens;

import com.moneyfi.apigateway.model.SessionTokenModel;

public interface SessionToken {

    void save(SessionTokenModel sessionTokenModel);

    SessionTokenModel getUserByUsername(String username);

    SessionTokenModel getSessionDetailsByToken(String token);
}
