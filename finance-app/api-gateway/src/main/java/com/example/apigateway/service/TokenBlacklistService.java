package com.example.apigateway.service;

import com.example.apigateway.model.BlackListedToken;
import com.example.apigateway.repository.TokenBlackListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenBlacklistService {
    @Autowired
    private TokenBlackListRepository tokenBlacklistRepository;

    public void blacklistToken(BlackListedToken blackListedToken) {
        tokenBlacklistRepository.save(blackListedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        BlackListedToken token1 = tokenBlacklistRepository.findByToken(token);
        if(token1 == null){
            return false;
        }
        return true;
    }
}

