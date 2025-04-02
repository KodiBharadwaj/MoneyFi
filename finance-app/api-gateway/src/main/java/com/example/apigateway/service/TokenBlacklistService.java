package com.example.apigateway.service;

import com.example.apigateway.model.BlackListedToken;
import com.example.apigateway.repository.TokenBlackListRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Scheduled(fixedRate = 3600000) // Runs every 1 hour
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Checking for expired tokens at: " + now);
        tokenBlacklistRepository.deleteByExpiryBefore(now);  // Deletes expired tokens
    }
}

