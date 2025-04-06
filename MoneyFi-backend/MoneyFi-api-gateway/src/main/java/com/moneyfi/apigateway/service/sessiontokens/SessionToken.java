package com.moneyfi.apigateway.service.sessiontokens;

import com.moneyfi.apigateway.model.SessionTokenModel;

public interface SessionToken {

    public void save(SessionTokenModel sessionTokenModel);

    public SessionTokenModel getUserByUsername(String username);

    public SessionTokenModel getSessionDetailsByToken(String token);
}
