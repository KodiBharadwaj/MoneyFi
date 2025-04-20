package com.moneyfi.apigateway.service.sessiontokens;

import com.moneyfi.apigateway.model.auth.SessionTokenModel;
import com.moneyfi.apigateway.repository.auth.SessionTokenRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionTokenImplementation implements SessionToken{

    private final SessionTokenRepository sessionTokenRepository;

    public SessionTokenImplementation(SessionTokenRepository sessionTokenRepository){
        this.sessionTokenRepository = sessionTokenRepository;
    }

    @Override
    public void save(SessionTokenModel sessionTokenModel) {
        sessionTokenRepository.save(sessionTokenModel);
    }

    @Override
    public SessionTokenModel getUserByUsername(String username) {
        return sessionTokenRepository.findByUsername(username);
    }

    @Override
    public SessionTokenModel getSessionDetailsByToken(String token) {
        return sessionTokenRepository.getSessionTokenModelByToken(token);
    }
}
